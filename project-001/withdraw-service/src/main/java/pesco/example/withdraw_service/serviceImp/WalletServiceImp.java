package pesco.example.withdraw_service.serviceImp;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletRequest;
import pesco.example.withdraw_service.bootstrap.UserTransactionsAgent;
import pesco.example.withdraw_service.clients.BlackListServiceClient;
import pesco.example.withdraw_service.clients.EscrowServiceClient;
import pesco.example.withdraw_service.clients.HistoryServiceClient;
import pesco.example.withdraw_service.clients.NotificationServiceClient;
import pesco.example.withdraw_service.clients.RevenueServiceClient;
import pesco.example.withdraw_service.clients.UserServiceClient;
import pesco.example.withdraw_service.clients.WalletServiceClient;
import pesco.example.withdraw_service.dtos.DeductWalletRequestDTO;
import pesco.example.withdraw_service.dtos.HistoryDTO;
import pesco.example.withdraw_service.dtos.TransferWalletRequestDTO;
import pesco.example.withdraw_service.dtos.UserDTO;
import pesco.example.withdraw_service.dtos.WalletBalanceDTO;
import pesco.example.withdraw_service.dtos.WalletSectionDTO;
import pesco.example.withdraw_service.dtos.WithdrawHistoryRequestDTO;
import pesco.example.withdraw_service.enums.BanActions;
import pesco.example.withdraw_service.enums.CurrencyStructType;
import pesco.example.withdraw_service.enums.TransactionType;
import pesco.example.withdraw_service.exceptions.Error;
import pesco.example.withdraw_service.payloads.CreateEscrowRequest;
import pesco.example.withdraw_service.services.WalletService;
import pesco.example.withdraw_service.utils.IdGeneratorUtil;
import pesco.example.withdraw_service.utils.TransferBootstrap;
import pesco.wallet_service.grpc.CurrencyType;
import pesco.wallet_service.grpc.FindUserWalletPinRequest;
import pesco.wallet_service.grpc.FindUserWalletPinResponse;
import pesco.wallet_service.grpc.GetWalletByCurrencyRequest;
import pesco.wallet_service.grpc.WalletBalanceResponse;
import pesco.wallet_service.grpc.WalletDeductionRequest;
import pesco.wallet_service.grpc.WithdrawResponse;

@Service
public class WalletServiceImp implements WalletService {
    
    private final TransferBootstrap transferBootstrap;
    private final PasswordEncoder passwordEncoder;
    private final BlackListServiceClient blackListServiceClient;
    private final HistoryServiceClient historyServiceClient;
    private final NotificationServiceClient notificationServiceClient;
    private final RevenueServiceClient revenueServiceClient;
    private final UserTransactionsAgent userTransactionsAgent;
    private final UserServiceClient userServiceClient;
    private final WalletServiceClient walletServiceClient;
    private final HazelcastIdempotencyService idempotencyService;
    private final EscrowServiceClient escrowServiceClient;
    
    private static final String PREFIX = "NX";

    public WalletServiceImp(TransferBootstrap transferBootstrap, PasswordEncoder passwordEncoder, 
        BlackListServiceClient blackListServiceClient, HistoryServiceClient historyServiceClient, 
        NotificationServiceClient notificationServiceClient, RevenueServiceClient revenueServiceClient, 
        UserTransactionsAgent userTransactionsAgent, UserServiceClient userServiceClient, 
        WalletServiceClient walletServiceClient, HazelcastIdempotencyService idempotencyService, 
        EscrowServiceClient escrowServiceClient) {
        this.transferBootstrap = transferBootstrap;
        this.passwordEncoder = passwordEncoder;
        this.blackListServiceClient = blackListServiceClient;
        this.historyServiceClient = historyServiceClient;
        this.notificationServiceClient = notificationServiceClient;
        this.revenueServiceClient = revenueServiceClient;
        this.userTransactionsAgent = userTransactionsAgent;
        this.userServiceClient = userServiceClient;
        this.walletServiceClient = walletServiceClient;
        this.idempotencyService = idempotencyService;
        this.escrowServiceClient = escrowServiceClient; 
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ResponseEntity<?> processWithdraw(DeductWalletRequestDTO dto, String token, HttpServletRequest request) {
        System.out.println("Process 1 - Start processWithdraw");
        String idempotencyKey = dto.getIdempotencyKey();
        Map<String, Object> jsonResponse = new HashMap<>();
        HazelcastIdempotencyService.IdempotencyResult idempotencyResult = idempotencyService.checkAndSetProcessing(idempotencyKey);

        if (idempotencyResult.isInvalid()) {
            System.out.println("Process 2 - Idempotency key is INVALID, returning 400");
            return Error.createResponse("Invalid idempotency key", HttpStatus.BAD_REQUEST, idempotencyResult.getMessage());
        }
        System.out.println("Process 3 - Idempotency key is valid");

        if (idempotencyResult.isDuplicate()) {
            System.out.println("Process 4 - Duplicate transaction detected, returning 409");
            return Error.createResponse("Duplicate transaction", HttpStatus.CONFLICT, idempotencyResult.getMessage());
        }
        System.out.println("Process 5 - Not a duplicate");

        if (idempotencyResult.isCompleted()) {
            System.out.println("Process 6 - Already completed, returning cached response");
            return ResponseEntity.ok(idempotencyResult.getResponse());
        }
        System.out.println("Process 7 - Not completed yet, proceeding");

        try {
            if (dto.getSenderUserId() == null) {
                System.out.println("Process 8 - SenderUserId is NULL, returning 400");
                idempotencyService.clearKey(idempotencyKey);
                return Error.createResponse("UserId is require.*", HttpStatus.BAD_REQUEST, "UserId can not be empty");
            }
            System.out.println("Process 9 - SenderUserId OK: " + dto.getSenderUserId());

            if (dto.getWalletId() == null) {
                System.out.println("Process 10 - WalletId is NULL, returning 400");
                idempotencyService.clearKey(idempotencyKey);
                return Error.createResponse("WalletId is require.*", HttpStatus.BAD_REQUEST, "WalletId can not be empty");
            }
            System.out.println("Process 11 - WalletId OK: " + dto.getWalletId());

            if (dto.getRecipientUsername().isEmpty() || dto.getRecipientUsername().isBlank()) {
                System.out.println("Process 12 - RecipientUsername is EMPTY, returning 400");
                idempotencyService.clearKey(idempotencyKey);
                return Error.createResponse("Recipient username is require.*", HttpStatus.BAD_REQUEST, "Recipient username can not be empty");
            }
            System.out.println("Process 13 - RecipientUsername OK: " + dto.getRecipientUsername());

            if (dto.getAmount() != null && dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("Process 14 - Amount is INVALID (<= 0): " + dto.getAmount());
                idempotencyService.clearKey(idempotencyKey);
                return Error.createResponse("Amount is required and must be greater than zero.", HttpStatus.BAD_REQUEST, "Please provide a valid amount you want to deposit.");
            }
            System.out.println("Process 15 - Amount OK: " + dto.getAmount());

            if (dto.getCurrency().isEmpty() || dto.getCurrency().isBlank()) {
                System.out.println("Process 16 - Currency is EMPTY, returning 400");
                idempotencyService.clearKey(idempotencyKey);
                return Error.createResponse("Currency Type is require.*", HttpStatus.BAD_REQUEST, "Currency Type can not be empty");
            } else if (!Arrays.stream(CurrencyType.values()).anyMatch(ct -> ct.name().equalsIgnoreCase(dto.getCurrency()))) {
                System.out.println("Process 17 - Currency is INVALID: " + dto.getCurrency());
                idempotencyService.clearKey(idempotencyKey);
                return Error.createResponse("Invalid Currency provided.*", HttpStatus.BAD_REQUEST,
                        "Please provide Currency type. Any of this list (USD, EUR, NGN, GBP, JPY, AUD, CAD, CHF, CNY, INR)");
            }
            System.out.println("Process 18 - Currency OK: " + dto.getCurrency());

            String providedPin = dto.getPassword().trim();
            if (providedPin == null || providedPin.isEmpty()) {
                System.out.println("Process 19 - Transfer pin is EMPTY, returning 400");
                idempotencyService.clearKey(idempotencyKey);
                return Error.createResponse("Transfer Pin is require.*", HttpStatus.BAD_REQUEST, "Please provide your transfer pin to your wallet.");
            }
            System.out.println("Process 20 - Transfer pin provided");

            System.out.println("Process 21 - Fetching sender user: " + dto.getUsername());
            UserDTO fromUser = userServiceClient.findByUsername(dto.getUsername(), token);
            if (fromUser == null) {
                System.out.println("Process 22 - Sender user NOT FOUND, returning 400");
                idempotencyService.clearKey(idempotencyKey);
                return Error.createResponse("Sender User not found", HttpStatus.BAD_REQUEST, "Your username is not found in our system.");
            }
            System.out.println("Process 23 - Sender user found: id=" + fromUser.getId() + ", username=" + fromUser.getUsername());

            if (dto.getRecipientUsername().equals(fromUser.getUsername())) {
                System.out.println("Process 24 - Sender and recipient are the SAME USER, returning 400");
                idempotencyService.clearKey(idempotencyKey);
                return Error.createResponse("Sorry.!, It seems you made a mistake...", HttpStatus.BAD_REQUEST, "You can transfer to yourself.");
            }
            System.out.println("Process 25 - Sender and recipient are different users");

            System.out.println("Process 26 - Fetching recipient user: " + dto.getRecipientUsername());
            UserDTO recipientUser = userServiceClient.findByUsername(dto.getRecipientUsername(), token);
            if (recipientUser == null) {
                System.out.println("Process 27 - Recipient user NOT FOUND, returning 400");
                idempotencyService.clearKey(idempotencyKey);
                return Error.createResponse("User not found", HttpStatus.BAD_REQUEST, "Recipient user is does not found in our system.");
            }
            System.out.println("Process 28 - Recipient user found: id=" + recipientUser.getId() + ", username=" + recipientUser.getUsername());

            if (fromUser.getId() != null && !fromUser.getUsername().equals(dto.getUsername())) {
                System.out.println("Process 29 - FRAUDULENT action detected (username mismatch), returning 400");
                idempotencyService.clearKey(idempotencyKey);
                return Error.createResponse("Fraudulent action detected.", HttpStatus.BAD_REQUEST,
                        "Fraudulent action detected. You are not authorized to operate this wallet.");
            }
            System.out.println("Process 30 - Sender identity verified");

            boolean isLockedAccount = fromUser.getId() != null ? fromUser.getRecords().get(0).isLocked() : false;
            boolean isBlockedAccount = fromUser.getId() != null ? fromUser.getRecords().get(0).isIsBlocked() : false;
            System.out.println("Process 31 - Account status: locked=" + isLockedAccount + ", blocked=" + isBlockedAccount);

            if (isLockedAccount) {
                System.out.println("Process 32 - Account is LOCKED, returning 400");
                idempotencyService.clearKey(idempotencyKey);
                return Error.createResponse("Account has been locked.", HttpStatus.BAD_REQUEST,
                        "Your account has been temporarily locked.");
            }

            if (isBlockedAccount) {
                System.out.println("Process 33 - Account is BLOCKED, returning 400");
                idempotencyService.clearKey(idempotencyKey);
                return Error.createResponse("Account has been blocked.", HttpStatus.BAD_REQUEST,
                        "Your account has been blocked.");
            }
            System.out.println("Process 34 - Account is active (not locked or blocked)");

            System.out.println("Process 35 - Checking blacklist for walletId: " + dto.getWalletId());
            if (blackListServiceClient.FindByWalletId(dto.getWalletId(), token)) {
                System.out.println("Process 36 - Wallet is BLACKLISTED, returning 403");
                return Error.createResponse("Transaction blocked due to blacklisted wallet address.", HttpStatus.FORBIDDEN, "Please contact support.");
            }
            System.out.println("Process 37 - Wallet is not blacklisted");

            System.out.println("Process 38 - Checking for high volume / frequent transactions");
            if (userTransactionsAgent.isHighVolumeOrFrequentTransactions(fromUser.getId(), fromUser.getEmail(),
                    fromUser.getRecords().get(0).getFirstName(), fromUser.getRecords().get(0).getLastName(),
                    dto.getWalletId(), token)) {
                System.out.println("Process 39 - HIGH VOLUME or FREQUENT transactions detected, returning 400");
                idempotencyService.clearKey(idempotencyKey);
                return Error.createResponse("Account temporarily banned.", HttpStatus.BAD_REQUEST,
                        "Account temporarily banned due to suspicious activity.");
            }
            System.out.println("Process 40 - No high volume/frequent transaction issues");

            System.out.println("Process 41 - Checking new account high-risk");
            if (userTransactionsAgent.isNewAccountAndHighRisk(fromUser.getUsername(), token)) {
                System.out.println("Process 42 - NEW ACCOUNT HIGH RISK detected, returning 400");
                idempotencyService.clearKey(idempotencyKey);
                return Error.createResponse("Your account has been restricted.", HttpStatus.BAD_REQUEST,
                        "New Account Restrictions. Please verify your identity to continue.");
            }
            System.out.println("Process 43 - New account risk check passed");

            System.out.println("Process 44 - Checking fraudulent action in recent history");
            if (isFradulentActionByUserInRecentHistory(fromUser.getId(), fromUser.getUsername(), TransactionType.DEPOSIT)) {
                System.out.println("Process 45 - FRAUDULENT action in recent history detected, locking account");
                idempotencyService.clearKey(idempotencyKey);
                userServiceClient.updateUserAccountStatus(fromUser.getId(), BanActions.SUSPICIOUS_ACTIVITY, token);
                return Error.createResponse("Account Locked.", HttpStatus.LOCKED,
                        "Your account has been found taking fradulent action.");
            }
            System.out.println("Process 46 - No fraudulent history found");

            CurrencyType currency = CurrencyType.valueOf(dto.getCurrency().toUpperCase());
            System.out.println("Process 47 - Fetching sender account balance for currency: " + currency);
            WalletBalanceDTO senderAccountBalance = getCurrentBalance(dto.getSenderUserId(), currency);
            System.out.println("Process 48 - Sender balance: " + (senderAccountBalance != null ? senderAccountBalance.getBalance() : "null"));

            System.out.println("Process 49 - Checking fraudulent behavior via agent");
            if (fromUser.getId() != null && userTransactionsAgent.isFraudulentBehavior(dto.getWalletId(), fromUser.getEmail(),
                    fromUser.getRecords().get(0).getFirstName(), fromUser.getRecords().get(0).getLastName(), token)) {
                System.out.println("Process 50 - FRAUDULENT BEHAVIOR flagged, returning 400");
                idempotencyService.clearKey(idempotencyKey);
                return Error.createResponse("This account has been flagged.", HttpStatus.BAD_REQUEST,
                        "Fraudulent Activity Detected. Please contact support team immediately.");
            }
            System.out.println("Process 51 - Fraud behavior check passed");

            System.out.println("Process 52 - Fetching recipient wallet account");
            WalletSectionDTO recipientWalletAccount = fromUser.getId() != null && recipientUser.getId() != null
                    ? walletServiceClient.getWalletSectionByUser(recipientUser.getId())
                    : null;
            System.out.println("Process 53 - Recipient wallet account: " + (recipientWalletAccount != null ? "found" : "null"));

            if (recipientWalletAccount == null && recipientUser.getId() != null) {
                System.out.println("Process 54 - Recipient has no wallet, creating one async");
                idempotencyService.clearKey(idempotencyKey);
                CompletableFuture<Void> createNewWallet = CompletableFuture
                        .runAsync(() -> walletServiceClient.createUserWallet(recipientUser.getId()));
                createNewWallet.join();
                System.out.println("Process 55 - Recipient wallet created");
            }

            System.out.println("Process 56 - Fetching wallet PIN settings for walletId: " + dto.getWalletId());
            FindUserWalletPinRequest pinRequest = FindUserWalletPinRequest.newBuilder()
                    .setWalletId(dto.getWalletId())
                    .build();
            FindUserWalletPinResponse walletSettings = walletServiceClient.findUserWalletPin(pinRequest);
            System.out.println("Process 57 - Wallet settings: " + (walletSettings != null ? "found, isSecure=" + walletSettings.getIsSecure() : "null"));

            if (walletSettings != null) {
                if (walletSettings.getIsSecure()) {
                    System.out.println("Process 58 - Wallet is secure, validating PIN");
                    if (senderAccountBalance != null && !passwordEncoder.matches(providedPin, walletSettings.getPassword())) {
                        System.out.println("Process 59 - PIN is INVALID, returning 400");
                        idempotencyService.clearKey(idempotencyKey);
                        return Error.createResponse("Invalid transfer pin.", HttpStatus.BAD_REQUEST,
                                "Invalid transfer pin.\nThe provided transfer pin is incorrect.");
                    }
                    System.out.println("Process 60 - PIN is VALID");
                } else {
                    System.out.println("Process 61 - Wallet PIN is NOT SET, returning 400");
                    idempotencyService.clearKey(idempotencyKey);
                    return Error.createResponse("Tranfer pin is not set.", HttpStatus.BAD_REQUEST,
                            "Please set your withdrawal pin before attempting to any withdraws.");
                }
            }

            System.out.println("Process 62 - Entering synchronized block for wallet withdraw");
            synchronized (("wallet-withdraw-" + dto.getSenderUserId() + "-" + (recipientUser.getId() != null ? recipientUser.getId() : null)).intern()) {
                System.out.println("Process 63 - Inside synchronized block");

                BigDecimal feeAmount = transferBootstrap.calculateFee(dto.getAmount());
                BigDecimal cAmount = dto.getAmount();
                BigDecimal finalDeduction = cAmount.add(feeAmount);
                BigDecimal wallet_balance = new BigDecimal((senderAccountBalance != null ? senderAccountBalance.getBalance() : "0.00"));
                System.out.println("Process 64 - Fee: " + feeAmount + ", Amount: " + cAmount + ", FinalDeduction: " + finalDeduction + ", WalletBalance: " + wallet_balance);

                if (wallet_balance.compareTo(finalDeduction) < 0) {
                    System.out.println("Process 65 - INSUFFICIENT balance, returning 400");
                    return Error.createResponse("Insufficient balancen.", HttpStatus.BAD_REQUEST, "Low balance");
                }
                System.out.println("Process 66 - Balance is sufficient");

                BigDecimal recipientPreviousBalance = (recipientWalletAccount != null && recipientWalletAccount.getWalletBalances() != null)
                        ? recipientWalletAccount.getWalletBalances().stream()
                            .filter(b -> dto.getCurrency().equalsIgnoreCase(b.getCurrency_code()))
                            .map(b -> new BigDecimal(b.getBalance().replace(",", "")))
                            .findFirst()
                            .orElse(BigDecimal.ZERO)
                        : BigDecimal.ZERO;
                System.out.println("Process 67 - Recipient previous balance: " + recipientPreviousBalance);

                jsonResponse.put("currency", recipientWalletAccount);
                String newAmount = dto.getAmount().toString();
                CurrencyType currencyTypes = CurrencyType.valueOf(dto.getCurrency().toUpperCase());

                System.out.println("Process 68 - Calling deduction for senderId: " + dto.getSenderUserId());
                WithdrawResponse withdrawResponse = deduction(
                        dto.getSenderUserId(),
                        dto.getWalletId(),
                        currencyTypes,
                        newAmount,
                        dto.getRecipientUsername(),
                        token);
                System.out.println("Process 69 - Deduction response: status=" + withdrawResponse.getStatus() + ", message=" + withdrawResponse.getMessage());

                if (!"success".equals(withdrawResponse.getStatus())) {
                    System.out.println("Process 70 - Deduction FAILED, clearing key and returning 500");
                    idempotencyService.clearKey(idempotencyKey);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of("status", "error", "message", withdrawResponse.getMessage()));
                }
                System.out.println("Process 71 - Deduction SUCCESS, new balance: " + withdrawResponse.getNewBalance());

                System.out.println("Process 72 - Creating escrow request");
                CreateEscrowRequest escrowRequest = new CreateEscrowRequest();
                escrowRequest.setSenderId(dto.getSenderUserId());
                escrowRequest.setRecipientId(recipientUser.getId());
                escrowRequest.setDescription("Withdrawal escrow for " + dto.getRecipientUsername());
                escrowRequest.setCurrencyCode(dto.getCurrency());
                escrowRequest.setAmount(dto.getAmount());

                ResponseEntity<?> escrowResponse = escrowServiceClient.create(escrowRequest);
                System.out.println("Process 73 - Escrow response status: " + escrowResponse.getStatusCode());

                if (!escrowResponse.getStatusCode().is2xxSuccessful()) {
                    System.out.println("Process 74 - Escrow creation FAILED, returning 500");
                    idempotencyService.clearKey(idempotencyKey);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of("status", "error", "message", "Failed to create escrow for withdrawal"));
                }
                System.out.println("Process 75 - Escrow created successfully");

                Map<String, Object> responseBody = (Map<String, Object>) escrowResponse.getBody();
                Map<String, Object> ledger = (Map<String, Object>) responseBody.get("ledger");
                String ledgerId = (String) ledger.get("id");
                System.out.println("Process 76 - Ledger ID: " + ledgerId + ", updating status to SUCCESS");
                escrowServiceClient.updateLedgerStatus(ledgerId, "SUCCESS");
                System.out.println("Process 77 - Ledger status updated");

                BigDecimal new_balance = new BigDecimal(withdrawResponse.getNewBalance());
                CurrencyStructType currencyType = CurrencyStructType.valueOf(dto.getCurrency().toUpperCase());

                StringBuilder descriptionBuilder = new StringBuilder("RF//FRM ");
                if (fromUser.getId() != null) {
                    String fromFirstName = fromUser.getRecords().get(0).getFirstName();
                    String fromLastName = fromUser.getRecords().get(0).getLastName();
                    descriptionBuilder.append((fromFirstName + " " + fromLastName).toUpperCase());
                }
                descriptionBuilder.append(" TO ");
                if (recipientUser.getId() != null) {
                    String toFirstName = recipientUser.getRecords().get(0).getFirstName();
                    String toLastName = recipientUser.getRecords().get(0).getLastName();
                    descriptionBuilder.append((toFirstName + " " + toLastName).toUpperCase());
                }
                descriptionBuilder.append("/MFY");
                String description1 = descriptionBuilder.toString();
                System.out.println("Process 78 - Sender description: " + description1);

                String transactionId = generateTransactionId();
                System.out.println("Process 79 - Generated transactionId: " + transactionId);

                String senderFirstname = capitalizeFirstLetter(fromUser.getRecords().get(0).getFirstName());
                String senderLastname = capitalizeFirstLetter(fromUser.getRecords().get(0).getLastName());
                String senderStringPreviousBalance = senderAccountBalance != null ? senderAccountBalance.getBalance() : null;
                BigDecimal senderPreviousBalance = new BigDecimal(senderStringPreviousBalance);
                System.out.println("Process 80 - Sender: " + senderFirstname + " " + senderLastname + ", prevBalance: " + senderPreviousBalance);

                System.out.println("Process 81 - Creating sender transaction history (async)");
                CompletableFuture<Void> senderHistory = createHistory(
                        cAmount, currencyType.name(), description1, dto.getNote(),
                        fromUser.getId(), dto.getWalletId(), TransactionType.DEBITED,
                        (recipientUser.getId() != null ? recipientUser.getId() : null),
                        (recipientWalletAccount != null ? recipientWalletAccount.getWalletId() : null),
                        (recipientUser.getId() != null ? recipientUser.getRecords().get(0).getFirstName() + " " + recipientUser.getRecords().get(0).getLastName() : null),
                        fromUser.getRecords().get(0).getFirstName() + " " + fromUser.getRecords().get(0).getLastName(),
                        (senderAccountBalance != null ? senderAccountBalance.getSymbol() : null),
                        transactionId, senderPreviousBalance, new_balance,
                        senderFirstname + ' ' + senderLastname, token, request);

                StringBuilder description2Builder = new StringBuilder();
                if (recipientUser.getId() != null) {
                    description2Builder.append((recipientUser.getRecords().get(0).getFirstName() + " " + recipientUser.getRecords().get(0).getLastName()).toUpperCase());
                }
                description2Builder.append("/Transfer from ");
                if (fromUser.getId() != null) {
                    description2Builder.append((fromUser.getRecords().get(0).getFirstName() + " " + fromUser.getRecords().get(0).getLastName()).toUpperCase());
                }
                String description2 = description2Builder.toString();
                System.out.println("Process 82 - Recipient description: " + description2);

                String recipientFirstname = capitalizeFirstLetter((recipientUser.getId() != null ? recipientUser.getRecords().get(0).getFirstName() : null));
                String recipientLastname = capitalizeFirstLetter((recipientUser.getId() != null ? recipientUser.getRecords().get(0).getLastName() : null));
                System.out.println("Process 83 - Recipient: " + recipientFirstname + " " + recipientLastname);

                System.out.println("Process 84 - Fetching updated recipient account balance");
                WalletBalanceDTO recipientAccountBalance = getCurrentBalance((recipientUser.getId() != null ? recipientUser.getId() : null), currency);
                WalletSectionDTO recipientNewBalanceWalletRequest = walletServiceClient.getWalletSectionByUser((recipientUser.getId() != null ? recipientUser.getId() : null));

                BigDecimal recipientNewBalance = recipientNewBalanceWalletRequest.getWalletBalances().stream()
                        .filter(b -> dto.getCurrency().equalsIgnoreCase(b.getCurrency_code()))
                        .map(b -> new BigDecimal(b.getBalance().replace(",", "")))
                        .findFirst()
                        .orElse(BigDecimal.ZERO);
                System.out.println("Process 85 - Recipient new balance: " + recipientNewBalance);

                System.out.println("Process 86 - Creating recipient transaction history (async)");
                CompletableFuture<Void> recipientHistory = createHistory(
                        cAmount, currencyType.name(), description2, dto.getNote(),
                        (recipientUser.getId() != null ? recipientUser.getId() : null),
                        (recipientWalletAccount != null ? recipientWalletAccount.getWalletId() : null),
                        TransactionType.CREDITED, fromUser.getId(), dto.getWalletId(),
                        fromUser.getUsername(),
                        (recipientUser.getId() != null ? recipientUser.getUsername() : null),
                        (senderAccountBalance != null ? senderAccountBalance.getSymbol() : null),
                        transactionId, recipientPreviousBalance, recipientNewBalance,
                        recipientFirstname + ' ' + recipientLastname, token, request);

                System.out.println("Process 87 - Waiting for both history futures to complete");
                CompletableFuture.allOf(senderHistory, recipientHistory).join();
                System.out.println("Process 88 - Both history records created");

                System.out.println("Process 89 - Crediting platform revenue, fee: " + feeAmount + " " + dto.getCurrency());
                revenueServiceClient.creditPlatformRevenue(feeAmount, dto.getCurrency());
                System.out.println("Process 90 - Platform revenue credited");

                System.out.println("Process 91 - Sending debit alert to: " + fromUser.getEmail());
                CompletableFuture<?> sendDebitAlert = CompletableFuture.supplyAsync(() ->
                        notificationServiceClient.sendDebitAlert(
                                fromUser.getEmail(), senderFirstname + " " + senderLastname,
                                recipientFirstname + " " + recipientLastname,
                                cAmount, currencyType.toString(), feeAmount, new_balance, transactionId, senderPreviousBalance));

                BigDecimal recipient_new_balance = new BigDecimal(recipientAccountBalance.getBalance());
                System.out.println("Process 92 - Sending credit alert to: " + (recipientUser.getId() != null ? recipientUser.getEmail() : "null"));
                CompletableFuture<?> sendCreditAlert = CompletableFuture.supplyAsync(() ->
                        notificationServiceClient.sendCreditAlert(
                                (recipientUser.getId() != null ? recipientUser.getEmail() : null),
                                senderFirstname + " " + senderLastname,
                                recipientFirstname + " " + recipientLastname,
                                cAmount, currencyType.toString(), recipient_new_balance, transactionId, recipientPreviousBalance));

                System.out.println("Process 93 - Waiting for both alert futures to complete");
                CompletableFuture.allOf(sendDebitAlert, sendCreditAlert).join();
                System.out.println("Process 94 - Both alerts sent");

                jsonResponse.put("status", withdrawResponse.getStatus());
                jsonResponse.put("message", withdrawResponse.getMessage());
                jsonResponse.put("currency", withdrawResponse.getCurrency());
                jsonResponse.put("newBalance", transferBootstrap.formatBigDecimal(new_balance));
                jsonResponse.put("timestamp", System.currentTimeMillis());

                System.out.println("Process 95 - Marking idempotency key as completed");
                idempotencyService.markAsCompleted(idempotencyKey, jsonResponse);

                System.out.println("Process 96 - SUCCESS, returning 200 OK");
                return ResponseEntity.ok(jsonResponse);
            }

        } catch (Exception e) {
            System.out.println("Process ERROR - Exception caught: " + e.getClass().getSimpleName() + " | Message: " + e.getMessage());
            e.printStackTrace();
            idempotencyService.clearKey(idempotencyKey);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }
            
    private CompletableFuture<Void> createHistory(
        BigDecimal amount,
        String currencyType,
        String description,
        String note,
        Long userId,
        Long walletId,
        TransactionType type,
        Long recipientUserId,
        Long recipientWalletId,
        String recipientUsername,
        String senderUsername,
        String symbol,
        String transactionId,
        BigDecimal previousBalance,
        BigDecimal newBalance,
        String fullname,
        String token,
        HttpServletRequest request) {

        WithdrawHistoryRequestDTO historyRequest = new WithdrawHistoryRequestDTO();
        
        // Set auto-generated IDs
        historyRequest.setSessionId(IdGeneratorUtil.generateSessionId());
        historyRequest.setTransactionId(IdGeneratorUtil.generateTransactionId());
        historyRequest.setReferenceNo(IdGeneratorUtil.generateReferenceNo());
        historyRequest.setTerminalId(IdGeneratorUtil.generateTerminalId());
        historyRequest.setErId(IdGeneratorUtil.generateErId());
        historyRequest.setTimestamp(IdGeneratorUtil.getCurrentTimestamp());
        historyRequest.setIpAddress(IdGeneratorUtil.getClientIpAddress(request));
        
        // Set existing fields
        historyRequest.setAmount(amount);
        historyRequest.setCurrencyType(currencyType);
        historyRequest.setDescription(description);
        historyRequest.setMessage("Transfer " + symbol + formatBigDecimal(amount) + " to " + recipientUsername);
        historyRequest.setUserId(userId);
        historyRequest.setWalletId(walletId);
        historyRequest.setType(type.name().toUpperCase());
        historyRequest.setRecipientUserId(recipientUserId);
        historyRequest.setRecipientWalletId(recipientWalletId);
        historyRequest.setReceiverFullName(recipientUsername);
        historyRequest.setSenderFullName(senderUsername);
        historyRequest.setPreviousBalance(previousBalance);
        historyRequest.setAvailableBalance(newBalance);
        historyRequest.setFullname(fullname);
        historyRequest.setStatus("SUCCESS");
        
        historyServiceClient.createUserCreditHistory(historyRequest, token);

        return CompletableFuture.completedFuture(null);
    }

    private WithdrawResponse deduction(Long userId, Long walletId, CurrencyType currency, String amount,
            String recipientUser, String token) {
        WalletDeductionRequest preparePayload = WalletDeductionRequest.newBuilder()
            .setUserId(userId)
            .setWalletId(walletId)
            .setCurrency(currency)
            .setAmount(amount)
            .setRecipientUsername(recipientUser)
            .setToken(token)
        .build();
        WithdrawResponse withdrawResponse = walletServiceClient.walletDeduct(preparePayload);
        return withdrawResponse;

    }

    private boolean isFradulentActionByUserInRecentHistory(Long id, String username, TransactionType deposit) {
        List<HistoryDTO> activities = historyServiceClient.listHistoryByType(id, username, deposit);

        if (activities == null || activities.isEmpty()) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMinuteAgo = now.minusMinutes(1);
        
        for (HistoryDTO activity : activities) {
            LocalDateTime activityTime = activity.getCreatedOn();
            if (activityTime.isAfter(oneMinuteAgo) && activityTime.isBefore(now)) {
                return true;
            }
        }
        
        return false;
    }

    private WalletBalanceDTO getCurrentBalance(Long userId, CurrencyType currency) {
        GetWalletByCurrencyRequest request = GetWalletByCurrencyRequest.newBuilder()
                .setUserId(userId)
                .setCurrency(currency)
                .build();
        
        try {
            // Use retry for more resilience
            WalletBalanceResponse grpcResponse = walletServiceClient.getWalletByCurrencyWithRetry(request, 3);

            // Map to DTO
            return new WalletBalanceDTO(
                    grpcResponse.getCurrencyCode(),
                    grpcResponse.getSymbol(),
                    grpcResponse.getBalance());
                    
        } catch (Exception e) {
            System.err.println("Failed to get balance for user " + userId + ": " + e.getMessage());
            throw new RuntimeException("Unable to fetch wallet balance. Please try again.", e);
    }
    }

    @SuppressWarnings("unchecked")
    @Override
    public ResponseEntity<?> processTransfer(TransferWalletRequestDTO request, String token) {
        String idempotencyKey = request.getIdempotencyKey(); 
        Map<String, Object> jsonResponse = new HashMap<>();
        HazelcastIdempotencyService.IdempotencyResult idempotencyResult = idempotencyService.checkAndSetProcessing(idempotencyKey);
        if (idempotencyResult.isInvalid()) {
            return Error.createResponse("Invalid idempotency key", HttpStatus.BAD_REQUEST,
                    idempotencyResult.getMessage());
        }
    
        if (idempotencyResult.isDuplicate()) {
            idempotencyService.clearKey(idempotencyKey); 
            return Error.createResponse("Duplicate transaction", HttpStatus.CONFLICT,
                    idempotencyResult.getMessage());
        }

        if (idempotencyResult.isCompleted()) {
            return ResponseEntity.ok(idempotencyResult.getResponse());
        }
        try {
            if (request.getSenderUserId() == null) {
                idempotencyService.clearKey(idempotencyKey); 
                return Error.createResponse("UserId is require.*", HttpStatus.BAD_REQUEST,
                        "UserId can not be empty");
            }

            if (request.getWalletId() == null) {
                idempotencyService.clearKey(idempotencyKey); 
                return Error.createResponse("WalletId is require.*", HttpStatus.BAD_REQUEST,
                        "WalletId can not be empty");
            }

            if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                idempotencyService.clearKey(idempotencyKey); 
                return Error.createResponse("Amount is required and must be greater than zero.", HttpStatus.BAD_REQUEST,
                        "Please provide a valid amount you want to deposit.");
            }

            if (request.getCurrency().isEmpty() || request.getCurrency().isBlank()) {
                idempotencyService.clearKey(idempotencyKey); 
                return Error.createResponse("Currency Type is require.*", HttpStatus.BAD_REQUEST,
                        "Currency Type can not be empty");
            } else if (!request.getCurrency().isEmpty() && !request.getCurrency().isBlank()) {
                if (!Arrays.stream(CurrencyType.values())
                        .anyMatch(ct -> ct.name().equalsIgnoreCase(request.getCurrency()))) {
                            idempotencyService.clearKey(idempotencyKey); 
                    return Error.createResponse("Invalid Currency provided.*", HttpStatus.BAD_REQUEST,
                            "Please provide Currency type. Any of this list (USD, EUR, NGN, GBP, JPY, AUD, CAD, CHF, CNY, INR)");
                }
            }

            String providedPin = request.getPassword().trim();
            if (providedPin == null || providedPin.isEmpty()) {
                idempotencyService.clearKey(idempotencyKey); 
                return Error.createResponse("Transfer Pin is require.*", HttpStatus.BAD_REQUEST,
                        "Please provide your transfer pin to your wallet.");
            }

            UserDTO fromUser = userServiceClient.findByUsername(request.getUsername(), token);
            if (fromUser == null) {
                idempotencyService.clearKey(idempotencyKey); 
                return Error.createResponse("Sender User not found", HttpStatus.BAD_REQUEST,
                        "Your username is not found in our system.");
            }

            if (fromUser.getId() != null && !fromUser.getUsername().equals(request.getUsername())) {
                idempotencyService.clearKey(idempotencyKey); 
                return Error.createResponse("Fraudulent action detected.", HttpStatus.BAD_REQUEST,
                        "Fraudulent action detected. You are not authorized to operate this wallet.\n One more attempt and you will be reported to the Economic and Financial Crimes Commission (EFCC).");
            }

            boolean isLockedAccount = fromUser.getId() != null ? fromUser.getRecords().get(0).isLocked() : null;
            boolean isBlockedAccount = fromUser.getId() != null ? fromUser.getRecords().get(0).isIsBlocked() : null;

            if (isLockedAccount) {
                idempotencyService.clearKey(idempotencyKey); 
                return Error.createResponse("Account has been locked.", HttpStatus.BAD_REQUEST,
                        "Your account has been temporarily locked.\nPlease contact support team to unlock your account.");
            }
            
            if (isBlockedAccount) {
                idempotencyService.clearKey(idempotencyKey); 
                return Error.createResponse("Account has been blocked.", HttpStatus.BAD_REQUEST,
                        "Your account has been blocked.\n Contact support team to process your account verifications.");
            }

            if (userTransactionsAgent.isHighVolumeOrFrequentTransactions(fromUser.getId(), fromUser.getEmail(), fromUser.getRecords().get(0).getFirstName(), fromUser.getRecords().get(0).getLastName(),  request.getWalletId(), token)) {
                idempotencyService.clearKey(idempotencyKey); 
                return Error.createResponse("Account temporarily banned.", HttpStatus.BAD_REQUEST,
                        "Account temporarily banned due to suspicious activity.\n Account temporarily banned due to suspicious activity. Please contact support team.");
            }

            if (userTransactionsAgent.isNewAccountAndHighRisk(fromUser.getUsername(), token)) {
                idempotencyService.clearKey(idempotencyKey); 
                return Error.createResponse("Your account has been restricted.", HttpStatus.BAD_REQUEST,
                        "New Account Restrictions\\n" + //
                                "\\n" + //
                                "New accounts have transaction limits for security reasons.\\n" + //
                                " Please verify your identity to continue.");
            }

            if (isFradulentActionByUserInRecentHistory(fromUser.getId(), fromUser.getUsername(),
                    TransactionType.DEPOSIT)) {
                idempotencyService.clearKey(idempotencyKey); 
                userServiceClient.updateUserAccountStatus(fromUser.getId(),
                        BanActions.SUSPICIOUS_ACTIVITY,
                        token);      
                return Error.createResponse("Account Locked.", HttpStatus.LOCKED,
                        "Your account has been found taking fradulent action.");
            }

            CurrencyType currency = CurrencyType.valueOf(request.getCurrency().toUpperCase());

            WalletBalanceDTO senderAccountBalance = getCurrentBalance(request.getSenderUserId(), currency);

            if (fromUser.getId() != null && userTransactionsAgent.isFraudulentBehavior(request.getWalletId(), fromUser.getEmail(), fromUser.getRecords().get(0).getFirstName(), fromUser.getRecords().get(0).getLastName(), token)) {
                idempotencyService.clearKey(idempotencyKey); 
                return Error.createResponse("This account has been flagged.", HttpStatus.BAD_REQUEST,
                        "Fraudulent Activity Detected\\n" + //
                                "\\n" + //
                                "Your account has been flagged for suspicious activity.\\n" + //
                                " Please contact support team immediately.");
            }

           if(blackListServiceClient.FindByWalletId(request.getWalletId(), token)){
                idempotencyService.clearKey(idempotencyKey); 
                return Error.createResponse("Transaction blocked due to blacklisted wallet address.",
                       HttpStatus.FORBIDDEN,
                       "Please contact support.");
           }

            FindUserWalletPinRequest pinRequest = FindUserWalletPinRequest.newBuilder()
                    .setWalletId(request.getWalletId())
                    .build();

            FindUserWalletPinResponse walletSettings = walletServiceClient.findUserWalletPin(pinRequest);

            if (walletSettings != null) {
                if (walletSettings.getIsSecure()) {
                    if (senderAccountBalance != null
                            && !passwordEncoder.matches(providedPin, walletSettings.getPassword())) {
                        idempotencyService.clearKey(idempotencyKey); 
                        return Error.createResponse("Invalid transfer pin.", HttpStatus.BAD_REQUEST,
                                "Invalid transfer pin.\nThe provided transfer pin is incorrect.");
                    }
                } else {
                    idempotencyService.clearKey(idempotencyKey); 
                    return Error.createResponse("Withdraw is not set.", HttpStatus.BAD_REQUEST,
                            "Please set your withdrawal pin before attempting to any withdraws.");
                }
            }


            List<HistoryDTO> activities = fromUser.getId() != null 
                    ? historyServiceClient.FindByTimestampAfterAndWalletId(
                            Instant.now().minus(1, ChronoUnit.MINUTES), 
                            fromUser.getId(), 
                            token
                    ) 
                    : null;

            if (activities != null) {
                for (HistoryDTO activity : activities) {
                    if (activity.getType() == TransactionType.DEPOSIT &&
                        activity.getTimestamp().isAfter(OffsetDateTime.now().minus(1, ChronoUnit.MINUTES))) {
                        if (fromUser.getId() != null) {
                            userServiceClient.updateUserAccountStatus(
                                fromUser.getId(),
                                BanActions.SUSPICIOUS_ACTIVITY,
                                token
                            );
                        }
                    }
                }
            }

            if (walletSettings != null) {
                if (walletSettings.getIsSecure()) {
                    if (senderAccountBalance != null && !passwordEncoder.matches(providedPin, walletSettings.getPassword())) {
                        idempotencyService.clearKey(idempotencyKey); 
                        return Error.createResponse("Invalid transfer pin.", HttpStatus.BAD_REQUEST,
                                "Invalid transfer pin.\nThe provided transfer pin is incorrect.");
                    }
                } else {
                    idempotencyService.clearKey(idempotencyKey); 
                    return Error.createResponse("Withdraw is not set.", HttpStatus.BAD_REQUEST,
                            "Please set your withdrawal pin before attempting to any withdraws.");
                }
            }

            WalletSectionDTO recipientWalletAccount = fromUser.getId() != null
                    ? walletServiceClient.getWalletSectionByUser(fromUser.getId())
                    : null;

            synchronized (("bank-transfer-" + request.getSenderUserId() + "-" + request.getAccountName()).intern()) {
                BigDecimal feeAmount = transferBootstrap.calculateFee(request.getAmount());
                BigDecimal cAmount = request.getAmount();
                BigDecimal finalDeduction = cAmount.add(feeAmount);
                BigDecimal wallet_balance = new BigDecimal(
                        (senderAccountBalance != null ? senderAccountBalance.getBalance() : ""));

                if (wallet_balance.compareTo(finalDeduction) < 0) {
                    idempotencyService.clearKey(idempotencyKey); 
                    return Error.createResponse("Insufficient balancen.", HttpStatus.BAD_REQUEST,
                        "Insufficient balance\nYour account balance is low.");
                }

                BigDecimal recipientPreviousBalance =
                (recipientWalletAccount != null && recipientWalletAccount.getWalletBalances() != null)
                    ? recipientWalletAccount.getWalletBalances().stream()
                        .filter(b -> request.getCurrency().equalsIgnoreCase(b.getCurrency_code()))
                        .map(b -> new BigDecimal(b.getBalance().replace(",", "")))
                        .findFirst()
                        .orElse(BigDecimal.ZERO)
                    : BigDecimal.ZERO;
                        jsonResponse.put("currency",recipientWalletAccount);
                String newAmount  = request.getAmount().toString();
                
                CurrencyType currencyTypes = CurrencyType.valueOf(request.getCurrency().toUpperCase());

                CreateEscrowRequest escrowRequest = new CreateEscrowRequest();
                escrowRequest.setSenderId(request.getSenderUserId());
                escrowRequest.setRecipientId(null);
                escrowRequest.setDescription("Transfer "+request.getAmount()+" to "+ request.getAccountName()+"/"+request.getAccountNumber());
                escrowRequest.setCurrencyCode(request.getCurrency());
                escrowRequest.setAmount(request.getAmount());
                
                ResponseEntity<?> escrowResponse = escrowServiceClient.create(escrowRequest);
                if (!escrowResponse.getStatusCode().is2xxSuccessful()) {
                    idempotencyService.clearKey(idempotencyKey);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of("status", "error", "message", "Failed to create escrow for withdrawal"));
                }

                Map<String, Object> responseBody = (Map<String, Object>) escrowResponse.getBody();
                Map<String, Object> ledger = (Map<String, Object>) responseBody.get("ledger");
                String ledgerId = (String) ledger.get("id");

                escrowServiceClient.updateLedgerStatus(ledgerId, "SUCCESS");
                
                // TransferToUserRequest  transferPayload = TransferToUserRequest.newBuilder()
                //     .setUserId(fromUser.getId())
                //     .setWalletId(request.getWalletId())
                //     .setCurrency(currency)
                //     .setRecipientBankAccountNumber(request.getAccountNumber())
                //     .setRecipientBankHolderName(request.getAccountName())
                //     .setRecipientBankName(request.getAccountName())
                //     .setAmount(finalDeduction.toString())
                // .build();

                // TransferResponse transferResponse = walletServiceClient.transferMoney(transferPayload);
          
                // BigDecimal new_balance = new BigDecimal(transferResponse.getNewBalance());

                // CurrencyStructType currencyType = CurrencyStructType
                //         .valueOf(request.getCurrency().toUpperCase());

                // String f_amount = transferBootstrap.formatBigDecimal(cAmount);
                // Symbols currencySymbols = Symbols.valueOf(currencyType.toString());
                // String symbol = currencySymbols.getSymbol();

                // WithdrawHistoryRequestDTO senderHistory = new WithdrawHistoryRequestDTO();
                // senderHistory.setAmount(cAmount.negate());
                // senderHistory.setCurrencyType(currencyType.toString());
                // senderHistory.setDescription("Transfered " + symbol + f_amount + " to "
                //         + (request.getAccountName() != null ? request.getAccountName() : null));
                // senderHistory.setType(TransactionType.DEBITED.toString());
                // senderHistory.setReceiverFullName(request.getAccountName()+ ' ' +request.getAccountName());
                // senderHistory.setUserId(fromUser.getId());
                // senderHistory.setSenderFullName(fromUser.getUsername());
                // senderHistory.setWalletId(request.getWalletId());
                // String transactionId = generateTransactionId();

                
                // You need to provide the previousBalance value
                // BigDecimal previousBalance =
                // (recipientWalletAccount != null && recipientWalletAccount.getWalletBalances() != null)
                //     ? recipientWalletAccount.getWalletBalances().stream()
                //         .filter(b -> request.getCurrency().equalsIgnoreCase(b.getCurrency_code()))
                //         .map(b -> new BigDecimal(b.getBalance().replace(",", "")))
                //         .findFirst()
                //         .orElse(BigDecimal.ZERO)
                //     : BigDecimal.ZERO;
                //     jsonResponse.put("currency",recipientWalletAccount);

                // CompletableFuture<?> sendDebitAlert = CompletableFuture
                //         .supplyAsync(() -> notificationServiceClient.sendDebitAlert(
                //                 fromUser.getEmail(),
                //                 fromUser.getUsername(),
                //                 request.getAccountName()+" "+request.getAccountName(),
                //                 cAmount,
                //                 currencyType.toString(),
                //                 feeAmount,
                //                 new_balance, 
                //                 transactionId,
                //                 previousBalance));
                // CompletableFuture.allOf(sendDebitAlert);

                // CompletableFuture<Void> CreateSenderHistory = CompletableFuture
                //         .runAsync(() -> historyServiceClient.createUserCreditHistory(senderHistory, token));
                
                // CompletableFuture.allOf(CreateSenderHistory);

                // revenueServiceClient.creditPlatformRevenue(feeAmount, request.getCurrency());

                
                idempotencyService.clearKey(idempotencyKey); 
                jsonResponse.put("status", HttpStatus.OK);
                jsonResponse.put("message", "Successfully transfer "+transferBootstrap.formatBigDecimal(request.getAmount())+" to "+request.getAccountName()+"/"+request.getAccountNumber());
                jsonResponse.put("currency", request.getCurrency());
                jsonResponse.put("newBalance", transferBootstrap.formatBigDecimal(recipientPreviousBalance));

                return ResponseEntity.ok(jsonResponse);
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    public String formatBigDecimal(BigDecimal amount) {
        String pattern = "#,##0.00";
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat(pattern, symbols);
        return decimalFormat.format(amount);
    }

    private String generateTransactionId() {
        long hash = Math.abs(UUID.randomUUID().getMostSignificantBits());
        String digits = String.valueOf(hash).substring(0, 9);
        return PREFIX + digits;
    }

    public static String capitalizeFirstLetter(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }
    
}
