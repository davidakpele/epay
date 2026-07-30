package com.example.auth_user_service.httpClients;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.grpc.client.GrpcChannelFactory;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.auth_user_service.dtos.WalletBalanceDTO;
import com.example.auth_user_service.dtos.WalletSectionDTO;
import com.example.auth_user_service.exceptions.WalletNotFoundException;
import com.example.auth_user_service.interfaces.IWalletServiceClient;
import com.example.auth_user_service.wallet.grpc.CreateWalletRequest;
import com.example.auth_user_service.wallet.grpc.CreateWalletResponse;
import com.example.auth_user_service.wallet.grpc.GetWalletByCurrencyRequest;
import com.example.auth_user_service.wallet.grpc.GetWalletSectionRequest;
import com.example.auth_user_service.wallet.grpc.WalletBalanceResponse;
import com.example.auth_user_service.wallet.grpc.WalletDeductionRequest;
import com.example.auth_user_service.wallet.grpc.WalletSectionResponse;
import com.example.auth_user_service.wallet.grpc.WalletServiceGrpc;
import com.example.auth_user_service.wallet.grpc.WithdrawResponse;
import io.grpc.StatusRuntimeException;

@Service
public class WalletServiceClient implements IWalletServiceClient {

    private final WalletServiceGrpc.WalletServiceBlockingStub walletServiceStub;
    private final Map<String, CompletableFuture<BigDecimal>> pendingRequests = new ConcurrentHashMap<>();

    public WalletServiceClient(GrpcChannelFactory channelFactory) {
        this.walletServiceStub = WalletServiceGrpc.newBlockingStub(
            channelFactory.createChannel("wallet-service")
        );
    }
    
    @Override
    public CompletableFuture<Map<String, Object>> createUserWallet(Long userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                CreateWalletRequest request = CreateWalletRequest.newBuilder()
                        .setUserId(userId)
                        .build();

                CreateWalletResponse response = walletServiceStub.createWallet(request);

                return Map.<String, Object>of(
                    "status", response.getStatus(),
                    "message", response.getMessage(),
                    "walletId", response.getWalletId()
                );
            } catch (StatusRuntimeException e) {
                throw new RuntimeException("gRPC wallet creation failed: " + e.getStatus().getDescription(), e);
            }
        });
    }

    @Override
    public WalletSectionDTO getWalletSectionByUser(Long userId) {
        GetWalletSectionRequest request = GetWalletSectionRequest.newBuilder()
                .setUserId(userId)
                .build();

        try {
            WalletSectionResponse grpcResponse = walletServiceStub.getWalletSectionByUser(request);

            List<WalletBalanceDTO> balances = grpcResponse.getWalletBalancesList().stream()
                    .map(balance -> new WalletBalanceDTO(
                            balance.getCurrencyCode(),
                            balance.getSymbol(),
                            balance.getBalance()
                    ))
                    .collect(Collectors.toList());

            return new WalletSectionDTO(
                    grpcResponse.getWalletId(),
                    grpcResponse.getHasTransferPin(),
                    balances
            );

        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == io.grpc.Status.Code.NOT_FOUND) {
                throw new WalletNotFoundException("Wallet not found for userId: " + userId);
            }
            throw new RuntimeException("gRPC error while fetching wallet section: " + e.getMessage(), e);
        }
    }

    @Override
    public WalletBalanceResponse getWalletByCurrency(GetWalletByCurrencyRequest request) {
        try {
            return walletServiceStub.getWalletByCurrency(request);
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == io.grpc.Status.Code.NOT_FOUND) {
                throw new WalletNotFoundException("Wallet or currency not found for userId: " + request.getUserId());
            }
            throw new RuntimeException("gRPC error while fetching balance by currency: " + e.getMessage(), e);
        }
    }

    @Override
    public WithdrawResponse walletDeduct(WalletDeductionRequest payloads) {
        return walletServiceStub.withdrawIn(payloads);
    }

    @Override
    public void onWebSocketMessage(String messageJson) {
        try {
            Map<String, Object> message = new ObjectMapper().readValue(messageJson, new TypeReference<>() {});
            if ("balance_response".equals(message.get("type"))) {
                String correlationId = (String) message.get("correlationId");
                CompletableFuture<BigDecimal> future = pendingRequests.remove(correlationId);
                if (future != null) {
                    String formatted = message.get("balance").toString();
                    BigDecimal parsedBalance = parseFormattedString(formatted);
                    future.complete(parsedBalance);
                }
            }
        } catch (JsonProcessingException | ParseException e) {
            // log this
        }
    }

    public static BigDecimal parseFormattedString(String formattedValue) throws ParseException {
        String pattern = "#,##0.00";
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat(pattern, symbols);
        Number number = decimalFormat.parse(formattedValue);
        return new BigDecimal(number.toString());
    }

    public Map<String, CompletableFuture<BigDecimal>> getPendingRequests() {
        return pendingRequests;
    }
}