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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
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
import pesco.example.withdraw_service.dtos.UserRecordDTO;
import pesco.example.withdraw_service.dtos.WalletBalanceDTO;
import pesco.example.withdraw_service.dtos.WalletSectionDTO;
import pesco.example.withdraw_service.enums.BanActions;
import pesco.example.withdraw_service.enums.TransactionType;
import pesco.example.withdraw_service.exceptions.Error;
import pesco.example.withdraw_service.exceptions.UserClientNotFoundException;
import pesco.example.withdraw_service.payloads.CreateEscrowRequest;
import pesco.example.withdraw_service.payloads.CreditHistoryRequest;
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
    
    @Override
    public ResponseEntity<?> processWithdraw(DeductWalletRequestDTO dto, String token) {
        String idempotencyKey = dto.getIdempotencyKey();

        // ── Idempotency gate ──────────────────────────────────────────────────────
        HazelcastIdempotencyService.IdempotencyResult idempotencyResult = idempotencyService.checkAndSetProcessing(idempotencyKey);
        if (idempotencyResult.isInvalid())   return Error.createResponse("Invalid idempotency key",  HttpStatus.BAD_REQUEST, idempotencyResult.getMessage());
        if (idempotencyResult.isDuplicate()) return Error.createResponse("Duplicate transaction",     HttpStatus.CONFLICT,    idempotencyResult.getMessage());
        if (idempotencyResult.isCompleted()) return ResponseEntity.ok(idempotencyResult.getResponse());

        try {
            // ── 1. Input validation — zero I/O, fail fast ─────────────────────────
            validateWithdrawRequest(dto, idempotencyKey);

            final String       currencyCode = dto.getCurrency().toUpperCase();
            final CurrencyType currency     = CurrencyType.valueOf(currencyCode);
            final String       providedPin  = dto.getPassword().trim();

            // ── 2. Sender + recipient lookup in PARALLEL ──────────────────────────
            CompletableFuture<UserDTO> senderFuture = CompletableFuture.supplyAsync(() -> {
                try { return userServiceClient.findByUsername(dto.getUsername(), token); }
                catch (UserClientNotFoundException e) { throw new RuntimeException("SENDER_NOT_FOUND:" + e.getMessage()); }
            });
            CompletableFuture<UserDTO> recipientFuture = CompletableFuture.supplyAsync(() -> {
                try { return userServiceClient.findByUsername(dto.getRecipientUsername(), token); }
                catch (UserClientNotFoundException e) { throw new RuntimeException("RECIPIENT_NOT_FOUND:" + e.getMessage()); }
            });

            final UserDTO fromUser, recipientUser;
            try {
                fromUser      = senderFuture.get();
                recipientUser = recipientFuture.get();
            } catch (ExecutionException e) {
                idempotencyService.clearKey(idempotencyKey);
                String msg = e.getCause().getMessage();
                if (msg.startsWith("SENDER_NOT_FOUND:"))    return Error.createResponse("Sender not found",    HttpStatus.BAD_REQUEST, msg.substring(17));
                if (msg.startsWith("RECIPIENT_NOT_FOUND:")) return Error.createResponse("Recipient not found", HttpStatus.BAD_REQUEST, msg.substring(20));
                throw e;
            }

            // ── 3. Fast in-memory business rule checks — no I/O ──────────────────
            if (dto.getRecipientUsername().equals(fromUser.getUsername())) {
                idempotencyService.clearKey(idempotencyKey);
                return Error.createResponse("Invalid recipient.", HttpStatus.BAD_REQUEST, "You cannot transfer to yourself.");
            }
            if (!fromUser.getUsername().equals(dto.getUsername())) {
                idempotencyService.clearKey(idempotencyKey);
                return Error.createResponse("Fraudulent action detected.", HttpStatus.FORBIDDEN, "You are not authorized to operate this wallet.");
            }

            UserRecordDTO senderRecord = fromUser.getRecords().get(0);
            if (senderRecord.isLocked()) {
                idempotencyService.clearKey(idempotencyKey);
                return Error.createResponse("Account locked.", HttpStatus.FORBIDDEN, "Your account has been temporarily locked.");
            }
            if (senderRecord.isIsBlocked()) {
                idempotencyService.clearKey(idempotencyKey);
                return Error.createResponse("Account blocked.", HttpStatus.FORBIDDEN, "Your account has been blocked.");
            }

            // ── 4. ALL security + balance + wallet + PIN checks in PARALLEL ───────
            CompletableFuture<Boolean> blacklistFuture     = CompletableFuture.supplyAsync(() -> blackListServiceClient.FindByWalletId(dto.getWalletId(), token));
            CompletableFuture<Boolean> highVolumeFuture    = CompletableFuture.supplyAsync(() ->
                    userTransactionsAgent.isHighVolumeOrFrequentTransactions(
                            fromUser.getId(), fromUser.getEmail(),
                            senderRecord.getFirstName(), senderRecord.getLastName(),
                            dto.getWalletId(), token));
            CompletableFuture<Boolean> newAccountFuture    = CompletableFuture.supplyAsync(() -> userTransactionsAgent.isNewAccountAndHighRisk(fromUser.getUsername(), token));
            CompletableFuture<Boolean> fraudHistoryFuture  = CompletableFuture.supplyAsync(() ->
                    isFradulentActionByUserInRecentHistory(fromUser.getId(), fromUser.getUsername(), TransactionType.DEPOSIT, token));
            CompletableFuture<Boolean> fraudBehaviorFuture = CompletableFuture.supplyAsync(() ->
                    userTransactionsAgent.isFraudulentBehavior(dto.getWalletId(), fromUser.getEmail(),
                            senderRecord.getFirstName(), senderRecord.getLastName(), token));

            CompletableFuture<WalletBalanceDTO>          senderBalanceFuture   = CompletableFuture.supplyAsync(() -> getCurrentBalance(dto.getSenderUserId(), currency));
            CompletableFuture<WalletSectionDTO>          recipientWalletFuture = CompletableFuture.supplyAsync(() -> walletServiceClient.getWalletSectionByUser(recipientUser.getId()));
            CompletableFuture<FindUserWalletPinResponse> pinFuture             = CompletableFuture.supplyAsync(() ->
                    walletServiceClient.findUserWalletPin(
                            FindUserWalletPinRequest.newBuilder().setWalletId(dto.getWalletId()).build()));

            // Single wait for ALL 8 parallel tasks
            CompletableFuture.allOf(
                    blacklistFuture, highVolumeFuture, newAccountFuture,
                    fraudHistoryFuture, fraudBehaviorFuture,
                    senderBalanceFuture, recipientWalletFuture, pinFuture
            ).join();

            // ── 5. Evaluate security results ──────────────────────────────────────
            if (blacklistFuture.get()) {
                idempotencyService.clearKey(idempotencyKey);
                return Error.createResponse("Transaction blocked.", HttpStatus.FORBIDDEN, "Please contact support.");
            }
            if (highVolumeFuture.get()) {
                idempotencyService.clearKey(idempotencyKey);
                return Error.createResponse("Account temporarily banned.", HttpStatus.BAD_REQUEST, "Suspicious activity detected.");
            }
            if (newAccountFuture.get()) {
                idempotencyService.clearKey(idempotencyKey);
                return Error.createResponse("Account restricted.", HttpStatus.BAD_REQUEST, "Please verify your identity to continue.");
            }
            if (fraudHistoryFuture.get()) {
                idempotencyService.clearKey(idempotencyKey);
                CompletableFuture.runAsync(() -> userServiceClient.updateUserAccountStatus(fromUser.getId(), BanActions.SUSPICIOUS_ACTIVITY, token));
                return Error.createResponse("Account locked.", HttpStatus.LOCKED, "Fraudulent action detected.");
            }
            if (fraudBehaviorFuture.get()) {
                idempotencyService.clearKey(idempotencyKey);
                return Error.createResponse("Account flagged.", HttpStatus.BAD_REQUEST, "Please contact support immediately.");
            }

            // ── 6. Unpack parallel results ────────────────────────────────────────
            WalletBalanceDTO          senderAccountBalance   = senderBalanceFuture.get();
            WalletSectionDTO          recipientWalletAccount = recipientWalletFuture.get();
            FindUserWalletPinResponse walletSettings         = pinFuture.get();

            // ── 7. PIN validation ─────────────────────────────────────────────────
            if (walletSettings == null || !walletSettings.getIsSecure()) {
                idempotencyService.clearKey(idempotencyKey);
                return Error.createResponse("Transfer pin not set.", HttpStatus.BAD_REQUEST,
                        "Please set your withdrawal pin before attempting any withdrawals.");
            }
            if (!passwordEncoder.matches(providedPin, walletSettings.getPassword())) {
                idempotencyService.clearKey(idempotencyKey);
                return Error.createResponse("Invalid transfer pin.", HttpStatus.BAD_REQUEST, "The provided transfer pin is incorrect.");
            }

            // ── 8. Recipient wallet missing — create async, reject with retry hint ─
            if (recipientWalletAccount == null) {
                idempotencyService.clearKey(idempotencyKey);
                CompletableFuture.runAsync(() -> walletServiceClient.createUserWallet(recipientUser.getId()));
                return Error.createResponse("Recipient wallet not found.", HttpStatus.BAD_REQUEST,
                        "Recipient wallet is being created. Please retry in a moment.");
            }

            // ── 9. Balance check + deduction — narrow synchronized block ──────────
            final BigDecimal feeAmount;
            final BigDecimal newSenderBalance;
            final BigDecimal recipientPreviousBalance = resolveBalance(recipientWalletAccount, currencyCode);
            final WithdrawResponse withdrawResponse;

            synchronized (("wallet-lock-" + dto.getSenderUserId()).intern()) {

                BigDecimal walletBalance  = new BigDecimal(senderAccountBalance != null ? senderAccountBalance.getBalance() : "0.00");
                feeAmount                 = transferBootstrap.calculateFee(dto.getAmount());
                BigDecimal finalDeduction = dto.getAmount().add(feeAmount);

                if (walletBalance.compareTo(finalDeduction) < 0) {
                    idempotencyService.clearKey(idempotencyKey);
                    return Error.createResponse("Insufficient balance.", HttpStatus.BAD_REQUEST, "Low balance.");
                }

                withdrawResponse = deduction(
                        dto.getSenderUserId(), dto.getWalletId(),
                        currency, dto.getAmount().toString(),
                        dto.getRecipientUsername(), token);
            }

            if (!"success".equals(withdrawResponse.getStatus())) {
                idempotencyService.clearKey(idempotencyKey);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("status", "error", "message", withdrawResponse.getMessage()));
            }

            newSenderBalance = new BigDecimal(withdrawResponse.getNewBalance());

            // ── 10. Escrow — sequential (create then update, dependent) ───────────
            CreateEscrowRequest escrowRequest = buildEscrowRequest(dto, recipientUser);
            ResponseEntity<?>   escrowResponse = escrowServiceClient.create(escrowRequest);

            if (!escrowResponse.getStatusCode().is2xxSuccessful()) {
                idempotencyService.clearKey(idempotencyKey);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("status", "error", "message", "Failed to create escrow."));
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> escrowBody = (Map<String, Object>) escrowResponse.getBody();
            @SuppressWarnings("unchecked")
            String ledgerId = (String) ((Map<String, Object>) escrowBody.get("ledger")).get("id");
            escrowServiceClient.updateLedgerStatus(ledgerId, "SUCCESS");

            // ── 11. Shared values ─────────────────────────────────────────────────
            UserRecordDTO recipientRecord  = recipientUser.getRecords().get(0);
            String senderFullName          = buildFullName(senderRecord.getFirstName(),    senderRecord.getLastName());
            String recipientFullName       = buildFullName(recipientRecord.getFirstName(), recipientRecord.getLastName());
            String transactionId           = generateTransactionId();
            String symbol                  = senderAccountBalance != null ? senderAccountBalance.getSymbol() : "";
            BigDecimal senderPrevBalance   = new BigDecimal(senderAccountBalance != null ? senderAccountBalance.getBalance() : "0.00");

            String senderDescription    = "RF//FRM " + senderFullName.toUpperCase() + " TO " + recipientFullName.toUpperCase() + "/MFY";
            String recipientDescription = recipientFullName.toUpperCase() + "/Transfer from " + senderFullName.toUpperCase();

            CompletableFuture<BigDecimal> recipientNewBalanceFuture = CompletableFuture.supplyAsync(() -> {
                WalletSectionDTO updated = walletServiceClient.getWalletSectionByUser(recipientUser.getId());
                return resolveBalance(updated, currencyCode);
            });


            CompletableFuture<Void> senderHistoryFuture = CompletableFuture.runAsync(() -> {
                CreditHistoryRequest senderHistory = buildCreditHistoryRequest(
                        /* amount          */ dto.getAmount(),
                        /* feeAmount       */ feeAmount,
                        /* currencyCode    */ currencyCode,
                        /* symbol          */ symbol,
                        /* description     */ senderDescription,
                        /* note            */ dto.getNote(),
                        /* type            */ TransactionType.DEBITED,
                        /* userId          */ fromUser.getId(),
                        /* walletId        */ dto.getWalletId(),
                        /* accountHolder   */ senderFullName,     
                        /* cpUserId        */ recipientUser.getId(),
                        /* cpWalletId      */ recipientWalletAccount.getWalletId(),
                        /* cpName          */ recipientFullName,
                        /* previousBalance */ senderPrevBalance,
                        /* newBalance      */ newSenderBalance,
                        /* transactionId   */ transactionId,
                        /* ipAddress        */ dto.getIpAddress(),
                        /* deviceId         */ dto.getDeviceId(),
                        /* GeoLocation      */ dto.getGeoLocation(),
                        /* userAgent       */ dto.getUserAgent()
                );
                historyServiceClient.createUserCreditHistory(senderHistory, token);
            });

            CompletableFuture<Void> recipientHistoryFuture = recipientNewBalanceFuture.thenAcceptAsync(recipientNewBalance -> {
                CreditHistoryRequest recipientHistory = buildCreditHistoryRequest(
                        /* amount          */ dto.getAmount(),
                        /* feeAmount       */ BigDecimal.ZERO,
                        /* currencyCode    */ currencyCode,
                        /* symbol          */ symbol,
                        /* description     */ recipientDescription,
                        /* note            */ dto.getNote(),
                        /* type            */ TransactionType.CREDITED,
                        /* userId          */ recipientUser.getId(),
                        /* walletId        */ recipientWalletAccount.getWalletId(),
                        /* accountHolder   */ recipientFullName,   
                        /* cpUserId        */ fromUser.getId(),
                        /* cpWalletId      */ dto.getWalletId(),
                        /* cpName          */ senderFullName, 
                        /* previousBalance */ recipientPreviousBalance,
                        /* newBalance      */ recipientNewBalance,
                        /* transactionId   */ transactionId,
                        /* ipAddress        */ dto.getIpAddress(),
                        /* deviceId         */ dto.getDeviceId(),
                        /* GeoLocation      */ dto.getGeoLocation(),
                         /* userAgent       */ dto.getUserAgent()
                );
                historyServiceClient.createUserCreditHistory(recipientHistory, token);
            });

            // ── 13. Fire-and-forget: revenue + alerts (do NOT block response) ─────
            BigDecimal recipientNewBalanceSnap = recipientNewBalanceFuture.get(); 

            CompletableFuture.runAsync(() -> revenueServiceClient.creditPlatformRevenue(feeAmount, currencyCode));
            CompletableFuture.runAsync(() -> notificationServiceClient.sendDebitAlert(
                    fromUser.getEmail(), senderFullName, recipientFullName,
                    dto.getAmount(), currencyCode, feeAmount, newSenderBalance,
                    transactionId, senderPrevBalance));
            CompletableFuture.runAsync(() -> notificationServiceClient.sendCreditAlert(
                    recipientUser.getEmail(), senderFullName, recipientFullName,
                    dto.getAmount(), currencyCode, recipientNewBalanceSnap,
                    transactionId, recipientPreviousBalance));

            // ── 14. Await both history writes before responding ───────────────────
            CompletableFuture.allOf(senderHistoryFuture, recipientHistoryFuture).join();

            // ── 15. Build + cache response ────────────────────────────────────────
            Map<String, Object> jsonResponse = new LinkedHashMap<>();
            jsonResponse.put("status",     withdrawResponse.getStatus());
            jsonResponse.put("message",    withdrawResponse.getMessage());
            jsonResponse.put("currency",   withdrawResponse.getCurrency());
            jsonResponse.put("newBalance", transferBootstrap.formatBigDecimal(newSenderBalance));
            jsonResponse.put("timestamp",  System.currentTimeMillis());

            idempotencyService.markAsCompleted(idempotencyKey, jsonResponse);
            return ResponseEntity.ok(jsonResponse);

        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            idempotencyService.clearKey(idempotencyKey);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    private CreditHistoryRequest buildCreditHistoryRequest(
            BigDecimal         amount,
            BigDecimal         feeAmount,
            String             currencyCode,
            String             symbol,
            String             description,
            String             note,
            TransactionType    type,
            Long               userId,
            Long               walletId,
            String             accountHolder,
            Long               counterpartyUserId,
            Long               counterpartyWalletId,
            String             counterpartyName,
            BigDecimal         previousBalance,
            BigDecimal         newBalance,
            String             transactionId,
            String             IpAddress,
            String             deviceId,
            String             geoLocation,
            String             userAgent
            ) {

        String now = Instant.now().toString();

        // Derive side-specific values from the transaction type
        boolean    isSender    = (type == TransactionType.DEBITED);
        BigDecimal taxAmount   = BigDecimal.ZERO;
        BigDecimal netAmount   = isSender
                ? amount.add(feeAmount).add(taxAmount).negate()
                : amount;
        String     dcFlag      = isSender ? "DEBIT"        : "CREDIT";
        String     ledgerType  = isSender ? "Transfer" : "Credit";

        CreditHistoryRequest h = new CreditHistoryRequest();

        // ── Core Identity ────────────────────────────────────────────
        h.setTransactionId(transactionId);
        h.setUserId(userId);
        h.setWalletId(walletId);
        h.setAccountHolder(accountHolder.toUpperCase());
        h.setSessionId(IdGeneratorUtil.generateSessionId());
        h.setReferenceId(null);
        h.setTerminalId(IdGeneratorUtil.generateTerminalId());
        h.setErId(IdGeneratorUtil.generateErId());

        // ── Transaction Info ─────────────────────────────────────────
        h.setType(type);
        h.setCurrencyType(currencyCode);
        h.setDescription(description);
        h.setMessage("Transfer " + symbol + formatBigDecimal(amount) + " to " + counterpartyName);
        h.setStatus("SUCCESS");
        h.setTimestamp(now);
        h.setProcessedAt(now);
        h.setApprovalTimestamp(null);
        h.setIpAddress(IpAddress);
        h.setUserAgent(userAgent);
        h.setDeviceId(deviceId);
        h.setGeoLocation(geoLocation);

        // ── Financial Amounts ────────────────────────────────────────
        h.setGrossAmount(amount);
        h.setFeeAmount(feeAmount);  
        h.setTaxAmount(taxAmount);
        h.setNetAmount(netAmount);
        h.setPreviousBalance(previousBalance);
        h.setAvailableBalance(newBalance);
        h.setRunningBalance(newBalance);

        // ── Double-Entry Accounting ──────────────────────────────────
        h.setDebitCredit(dcFlag);
        h.setLedgerEntryType(ledgerType);

        // ── Counterparty ─────────────────────────────────────────────
        h.setCounterpartyUserId(counterpartyUserId);
        h.setCounterpartyWalletId(counterpartyWalletId);
        h.setCounterpartyAccountHolder(counterpartyName.toUpperCase());
        h.setBankCode(null);
        h.setBankAccountNumber(null);
        h.setRoutingNumber(null);
        h.setExternalReference(null);

        // ── Multi-Currency ───────────────────────────────────────────
        h.setOriginalCurrency(currencyCode);
        h.setExchangeRate(BigDecimal.ONE);

        // ── Reversal & Disputes ──────────────────────────────────────
        h.setParentHistoryId(null);
        h.setReversalReason(null);
        h.setDisputeStatus(null);
        h.setDisputeReference(null);

        // ── Idempotency & Retry ──────────────────────────────────────
        h.setIdempotencyKey(UUID.randomUUID().toString());
        h.setRetryCount(0);
        h.setFailureReason(null);

        // ── Channel & Device ─────────────────────────────────────────
        h.setChannel("API");
        h.setDeviceId(deviceId);
        h.setUserAgent(userAgent);
        h.setGeoLocation(geoLocation);

        // ── Compliance & Risk ────────────────────────────────────────
        h.setRiskScore(BigDecimal.ZERO);
        h.setAmlFlag(false);
        h.setSanctionScreeningResult(null);
        h.setComplianceNote(null);
        h.setReviewedBy(null);

        // ── Admin Audit ──────────────────────────────────────────────
        h.setInitiatedBy(userId.toString());
        h.setApprovedBy(null);
        h.setAdminNote(type.name() + " of " + currencyCode + " " + formatBigDecimal(amount) + " via API");
        h.setManualAdjustmentFlag(false);

        // ── Metadata ─────────────────────────────────────────────────
        h.setCategory("TRANSFER");
        h.setTags(null);
        h.setNote(note);

        return h;
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

    private boolean isFradulentActionByUserInRecentHistory(Long id, String username, TransactionType deposit, String token) {
        List<HistoryDTO> activities = historyServiceClient.listHistoryByType(id, username, deposit, token);

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
            WalletBalanceResponse grpcResponse = walletServiceClient.getWalletByCurrencyWithRetry(request, 3);
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
                    TransactionType.DEPOSIT, token)) {
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
    
    private void validateWithdrawRequest(DeductWalletRequestDTO dto, String idempotencyKey) {
        if (dto.getSenderUserId() == null) {
            idempotencyService.clearKey(idempotencyKey);
            throw new IllegalArgumentException("UserId is required.");
        }
        if (dto.getWalletId() == null) {
            idempotencyService.clearKey(idempotencyKey);
            throw new IllegalArgumentException("WalletId is required.");
        }
        if (dto.getRecipientUsername() == null || dto.getRecipientUsername().isBlank()) {
            idempotencyService.clearKey(idempotencyKey);
            throw new IllegalArgumentException("Recipient username is required.");
        }
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            idempotencyService.clearKey(idempotencyKey);
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
        if (dto.getCurrency() == null || dto.getCurrency().isBlank()) {
            idempotencyService.clearKey(idempotencyKey);
            throw new IllegalArgumentException("Currency is required.");
        }
        if (Arrays.stream(CurrencyType.values()).noneMatch(ct -> ct.name().equalsIgnoreCase(dto.getCurrency()))) {
            idempotencyService.clearKey(idempotencyKey);
            throw new IllegalArgumentException("Invalid currency: " + dto.getCurrency());
        }
        if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
            idempotencyService.clearKey(idempotencyKey);
            throw new IllegalArgumentException("Transfer pin is required.");
        }
    }

    private BigDecimal resolveBalance(WalletSectionDTO walletSection, String currencyCode) {
        if (walletSection == null || walletSection.getWalletBalances() == null) return BigDecimal.ZERO;
        return walletSection.getWalletBalances().stream()
                .filter(b -> currencyCode.equalsIgnoreCase(b.getCurrency_code()))
                .map(b -> new BigDecimal(b.getBalance().replace(",", "")))
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }

    private String buildFullName(String firstName, String lastName) {
        return capitalizeFirstLetter(firstName) + " " + capitalizeFirstLetter(lastName);
    }

    private CreateEscrowRequest buildEscrowRequest(DeductWalletRequestDTO dto, UserDTO recipient) {
        CreateEscrowRequest escrow = new CreateEscrowRequest();
        escrow.setSenderId(dto.getSenderUserId());
        escrow.setRecipientId(recipient.getId());
        escrow.setDescription("Withdrawal escrow for " + dto.getRecipientUsername());
        escrow.setCurrencyCode(dto.getCurrency());
        escrow.setAmount(dto.getAmount());
        return escrow;
    }

}
