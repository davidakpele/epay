package com.pesco.wallet_service.handler;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import io.grpc.Status;
import com.google.protobuf.Timestamp;
import com.pesco.wallet_service.client.UserServiceClient;
import com.pesco.wallet_service.enums.Currency;
import com.pesco.wallet_service.models.CurrencyBalanceMapStruct;
import com.pesco.wallet_service.models.Wallet;
import com.pesco.wallet_service.models.WalletSettings;
import com.pesco.wallet_service.repository.WalletRepository;
import com.pesco.wallet_service.repository.WalletSettingsRepository;
import com.pesco.wallet_service.util.AccountWrapper;
import com.pesco.wallet_service.util.HazelcastWallet;
import com.pesco.wallet_service.util.JwtTokenProvider;
import pesco.wallet_service.grpc.GetWalletSectionRequest;
import pesco.wallet_service.grpc.ListWalletsRequest;
import pesco.wallet_service.grpc.ListWalletsResponse;
import pesco.wallet_service.grpc.UpdateBalanceRequest;
import pesco.wallet_service.grpc.WalletBalanceResponse;
import pesco.wallet_service.grpc.WalletDeductionRequest;
import io.grpc.stub.StreamObserver;
import jakarta.transaction.Transactional;
import pesco.wallet_service.grpc.CreateTransferPinRequest;
import pesco.wallet_service.grpc.CreateTransferPinResponse;
import pesco.wallet_service.grpc.CurrencyBalance;
import pesco.wallet_service.grpc.CurrencyType;
import pesco.wallet_service.grpc.FindUserWalletPinRequest;
import pesco.wallet_service.grpc.FindUserWalletPinResponse;
import pesco.wallet_service.grpc.GetWalletByCurrencyRequest;
import pesco.wallet_service.grpc.GetWalletRequest;
import pesco.wallet_service.grpc.WalletResponse;
import pesco.wallet_service.grpc.WalletSectionResponse;
import pesco.wallet_service.grpc.WalletServiceGrpc;
import pesco.wallet_service.grpc.WithdrawResponse;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

@GrpcService
public class WalletServiceImplementation extends WalletServiceGrpc.WalletServiceImplBase {

    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserServiceClient userServiceClient;
    private final WalletSettingsRepository walletSettingsRepository;
    private final HazelcastWallet redisWallet;
    private final JwtTokenProvider jwtprovider;
    
    public WalletServiceImplementation(WalletRepository walletRepository,
            PasswordEncoder passwordEncoder,
            UserServiceClient userServiceClient,
            AccountWrapper accountWrapper,
            WalletSettingsRepository walletSettingsRepository, 
            HazelcastWallet redisWallet, JwtTokenProvider jwtprovider) {
        this.walletRepository = walletRepository;
        this.passwordEncoder = passwordEncoder;
        this.userServiceClient = userServiceClient;
        this.walletSettingsRepository = walletSettingsRepository;
        this.redisWallet = redisWallet;
        this.jwtprovider = jwtprovider;
    }

    @Override
    public void createTransferPin(CreateTransferPinRequest request,
            StreamObserver<CreateTransferPinResponse> responseObserver) {
        try {
            String token = request.getToken();
            String providedPin = request.getRequest().getTransferPin();

            String extractedUserId = jwtprovider.getUserIdFromJWT(token);

            long userId = Long.parseLong(extractedUserId);

            Optional<Wallet> wallet = walletRepository.findByUserId(userId);

            if (wallet.isPresent()) {
                Wallet extractWallet = wallet.get();
                walletRepository.save(extractWallet);

                WalletSettings settings = new WalletSettings();
                settings.setPassword(passwordEncoder.encode(providedPin));
                settings.setWallet(extractWallet);
                settings.setIsSecure(true);
                walletSettingsRepository.save(settings);
            }

            boolean pinUpdated = userServiceClient.UpdateUserTranferPinInUserRecord(token, userId, true);

            if (pinUpdated) {
                CreateTransferPinResponse response = CreateTransferPinResponse.newBuilder()
                        .setMessage("Withdrawal/transfer password successfully set")
                        .setDetails("Success")
                        .setStatusCode(201)
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("Failed to update user transfer pin")
                        .asRuntimeException());
            }
        } catch (NumberFormatException e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Error creating transfer pin: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getWalletSectionByUser(GetWalletSectionRequest request,
            StreamObserver<WalletSectionResponse> responseObserver) {
        try {
           
            Long userId = request.getUserId();
            Optional<Wallet> walletOptional = walletRepository.findByUserId(userId);

            if (walletOptional.isEmpty()) {
                responseObserver.onError(Status.NOT_FOUND
                        .withDescription("Wallet not found")
                        .asRuntimeException());
                return;
            }

            Wallet wallet = walletOptional.get();

            Stream<CurrencyBalanceMapStruct> balanceStream = wallet.getBalances().stream();

            List<pesco.wallet_service.grpc.WalletBalanceDTO> grpcBalances = balanceStream
                    .map((Function<CurrencyBalanceMapStruct, pesco.wallet_service.grpc.WalletBalanceDTO>) balance -> pesco.wallet_service.grpc.WalletBalanceDTO
                            .newBuilder()
                            .setCurrencyCode(balance.getCurrencyCode())
                            .setSymbol(balance.getCurrencySymbol())
                            .setBalance(FormatBigDecimal(balance.getBalance()))
                            .build())
                    .collect(Collectors.toList());            

            Optional<WalletSettings> walletSettingsOptional = walletSettingsRepository.findByWalletId(wallet.getId());  
            boolean hasTransferPin = walletSettingsOptional
                    .map(WalletSettings::isIsSecure)
                    .orElse(false); 

            WalletSectionResponse response = WalletSectionResponse.newBuilder()
                    .addAllWalletBalances(grpcBalances)
                    .setWalletId(wallet.getId())
                    .setHasTransferPin(hasTransferPin)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Error retrieving wallet section: " + e.getMessage())
                    .asRuntimeException());
        }
    }    

    @Override
    public void getWalletByCurrency(GetWalletByCurrencyRequest request,
            StreamObserver<WalletBalanceResponse> responseObserver) {
        try {
            Long userId = request.getUserId();
            String currency = request.getCurrency().name();

            Optional<Wallet> walletOptional = walletRepository.findWalletByUserIdAndCurrencyCode(userId, currency);

            if (walletOptional.isEmpty()) {
                responseObserver.onError(Status.NOT_FOUND
                        .withDescription("Wallet or currency not found for user ID " + userId)
                        .asRuntimeException());
                return;
            }

            Wallet wallet = walletOptional.get();

            Optional<CurrencyBalanceMapStruct> currencyBalanceOpt = wallet.getBalances().stream()
                    .filter(balance -> balance.getCurrencyCode().equals(currency))
                    .findFirst();

            if (currencyBalanceOpt.isEmpty()) {
                responseObserver.onError(Status.NOT_FOUND
                        .withDescription("Currency " + currency + " not found in wallet")
                        .asRuntimeException());
                return;
            }

            CurrencyBalanceMapStruct balance = currencyBalanceOpt.get();

            WalletBalanceResponse response = WalletBalanceResponse.newBuilder()
                    .setWalletId(wallet.getId())
                    .setCurrencyCode(balance.getCurrencyCode())
                    .setSymbol(balance.getCurrencySymbol())
                    .setBalance(balance.getBalance().toString())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Error retrieving currency balance: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    @Transactional  
    public void withdrawIn(WalletDeductionRequest request, StreamObserver<WithdrawResponse> responseObserver) {
        try {
            Long userId = request.getUserId();
            CurrencyType currencyType = request.getCurrency();
            BigDecimal amount = new BigDecimal(request.getAmount());
            Currency currency = Currency.valueOf(currencyType.name());

            CompletableFuture<Wallet> senderFuture = CompletableFuture.supplyAsync(
                () -> walletRepository.findWalletByUserId(userId)
            );
            CompletableFuture<Long> recipientIdFuture = CompletableFuture.supplyAsync(
                () -> userServiceClient.getUserIdByUsername(request.getRecipientUsername(), request.getToken())
            );

            Wallet senderWallet = senderFuture.get();
            Long recipientUserId = recipientIdFuture.get();

            if (senderWallet == null) {
                responseObserver.onError(Status.NOT_FOUND.withDescription("Sender wallet not found").asRuntimeException());
                return;
            }

            CurrencyBalanceMapStruct senderCurrency = senderWallet.getBalances().stream()
                    .filter(b -> b.getCurrencyCode().equalsIgnoreCase(currencyType.name()))
                    .findFirst()
                    .orElse(null);

            if (senderCurrency == null || senderCurrency.getBalance().compareTo(amount) < 0) {
                responseObserver.onError(Status.FAILED_PRECONDITION.withDescription("Insufficient balance").asRuntimeException());
                return;
            }

            Wallet recipientWallet = walletRepository.findWalletByUserId(recipientUserId);

            senderCurrency.setBalance(senderCurrency.getBalance().subtract(amount));

            CurrencyBalanceMapStruct recipientCurrency = recipientWallet.getBalances().stream()
                    .filter(b -> b.getCurrencyCode().equalsIgnoreCase(currencyType.name()))
                    .findFirst()
                    .orElse(null);

            if (recipientCurrency == null) {
                responseObserver.onError(Status.NOT_FOUND.withDescription("Recipient currency not supported").asRuntimeException());
                return;
            }
            recipientCurrency.setBalance(recipientCurrency.getBalance().add(amount));
            walletRepository.saveAll(List.of(senderWallet, recipientWallet));

            CompletableFuture.runAsync(() -> {
                redisWallet.updateHazelcastWalletBalance(userId, currency, amount.negate());
                redisWallet.updateHazelcastWalletBalance(recipientUserId, currency, amount);
            }).exceptionally(ex -> null);

            WithdrawResponse response = WithdrawResponse.newBuilder()
                    .setStatus("success")
                    .setMessage("Amount transferred successfully.")
                    .setNewBalance(senderCurrency.getBalance().toPlainString())
                    .setCurrency(currencyType)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            responseObserver.onError(Status.INTERNAL.withDescription("Transfer failed: " + e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void findUserWalletPin(FindUserWalletPinRequest request, StreamObserver<FindUserWalletPinResponse> responseObserver) {
        Optional<Wallet> wallet = walletRepository.findById(request.getWalletId());
        if (wallet != null) {
        Optional<WalletSettings> walletSettings = walletSettingsRepository.findByWalletId(wallet.get().getId());
            if (walletSettings != null) {
                FindUserWalletPinResponse response = FindUserWalletPinResponse.newBuilder()
                    .setPassword(walletSettings.get().getPassword())
                    .setIsSecure(walletSettings.get().isIsSecure())
                    .setWalletId(walletSettings.get().getWallet().getId())
                    .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(Status.NOT_FOUND.withDescription("Wallet settings not found").asRuntimeException());
            }
        } else {
            responseObserver.onError(Status.NOT_FOUND.withDescription("Wallet not found").asRuntimeException());
        }
    }

    @Override
    public void getWallet(GetWalletRequest request, StreamObserver<WalletResponse> responseObserver) {
        try {
            Optional<Wallet> walletOptional;

            if (request.hasId()) {
                walletOptional = walletRepository.findById(request.getId());
            } else if (request.hasUserId()) {
                walletOptional = walletRepository.findByUserId(request.getUserId());
            } else {
                responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription("Wallet ID or User ID must be provided.")
                        .asRuntimeException());
                return;
            }

            if (walletOptional.isPresent()) {
                Wallet wallet = walletOptional.get();
                WalletResponse response = toWalletResponse(wallet);
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(Status.NOT_FOUND
                        .withDescription("Wallet not found.")
                        .asRuntimeException());
            }

        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Error retrieving wallet: " + e.getMessage())
                    .asRuntimeException());
        }
    }


    @Override
    @Transactional
    public void updateBalance(UpdateBalanceRequest request, StreamObserver<WalletResponse> responseObserver) {
        try {
            Wallet wallet = walletRepository.findById(request.getWalletId())
                    .orElse(null);

            if (wallet == null) {
                responseObserver.onError(Status.NOT_FOUND
                        .withDescription("Wallet not found.")
                        .asRuntimeException());
                return;
            }

            CurrencyBalance balanceUpdate = request.getBalanceUpdate();
            BigDecimal amountToAdd = new BigDecimal(balanceUpdate.getBalance());
            String currencyCode = balanceUpdate.getCurrencyCode().toUpperCase(); 

            Optional<CurrencyBalanceMapStruct> existing = wallet.getBalances().stream()
                    .filter(cb -> cb.getCurrencyCode().equalsIgnoreCase(currencyCode))
                    .findFirst();

            if (existing.isPresent()) {
                existing.get().setBalance(existing.get().getBalance().add(amountToAdd));
            } else {
                CurrencyBalanceMapStruct newCurrency = new CurrencyBalanceMapStruct();
                newCurrency.setCurrencyCode(currencyCode);
                newCurrency.setCurrencySymbol(balanceUpdate.getCurrencySymbol());
                newCurrency.setBalance(amountToAdd);
                wallet.getBalances().add(newCurrency);
            }

            Wallet updatedWallet = walletRepository.save(wallet);

            CompletableFuture.runAsync(() ->
                redisWallet.updateHazelcastWalletBalance(
                    wallet.getUserId(),
                    Currency.valueOf(currencyCode),
                    amountToAdd
                )
            ).exceptionally(ex -> null);

            responseObserver.onNext(toWalletResponse(updatedWallet));
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Error updating wallet balance: " + e.getMessage())
                    .asRuntimeException());
        }
    }
    
    @Override
    public void listWallets(ListWalletsRequest request, StreamObserver<ListWalletsResponse> responseObserver) {
        try {
            int page = request.getPage();
            int size = request.getSize();

            Pageable pageable = PageRequest.of(page, size);
            Page<Wallet> walletPage = walletRepository.findAll(pageable);

            ListWalletsResponse.Builder responseBuilder = ListWalletsResponse.newBuilder()
                    .setTotalPages(walletPage.getTotalPages())
                    .setCurrentPage(walletPage.getNumber());

            for (Wallet wallet : walletPage.getContent()) {
                responseBuilder.addWallets(toWalletResponse(wallet));
            }

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Error listing wallets: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    private WalletResponse toWalletResponse(Wallet wallet) {
        WalletResponse.Builder builder = WalletResponse.newBuilder()
            .setId(wallet.getId())
            .setUserId(wallet.getUserId())
            .setCreatedOn(toTimestamp(wallet.getCreatedOn()))
            .setUpdatedOn(toTimestamp(wallet.getUpdatedOn()));
    
        for (CurrencyBalanceMapStruct cb : wallet.getBalances()) {

            CurrencyBalance grpcBalance = CurrencyBalance.newBuilder()
                    .setCurrencyCode(cb.getCurrencyCode())
                    .setCurrencySymbol(cb.getCurrencySymbol())
                    .setBalance(cb.getBalance().toPlainString())
                    .build();

            builder.addBalances(grpcBalance);
        }
    
        return builder.build();
    }
    
    private Timestamp toTimestamp(LocalDateTime dateTime) {
        return Timestamp.newBuilder()
                .setSeconds(dateTime.toEpochSecond(ZoneOffset.UTC))
                .setNanos(dateTime.getNano())
                .build();
    }

    public static String FormatBigDecimal(BigDecimal amount) {
        String pattern = "#,##0.00";
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat(pattern, symbols);
        return decimalFormat.format(amount);
    }
}