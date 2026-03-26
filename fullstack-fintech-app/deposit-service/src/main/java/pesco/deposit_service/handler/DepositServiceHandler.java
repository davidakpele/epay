package pesco.deposit_service.handler;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import pesco.deposit_service.clients.HistoryServiceClient;
import pesco.deposit_service.clients.NotificationServiceClient;
import pesco.deposit_service.clients.WalletServiceClient;
import pesco.deposit_service.dto.WalletDTO;
import pesco.deposit_service.enums.TransactionType;
import pesco.deposit_service.payloads.DepositHistoryRequest;
import pesco.deposit_service.payloads.DepositRequest;
import pesco.deposit_service.services.DepositService;
import pesco.deposit_service.exceptions.BanKDetailsNotFound;
import pesco.deposit_service.exceptions.Error;


@Service
public class DepositServiceHandler implements DepositService {

    private final WalletServiceClient walletServiceClient;
    private final HistoryServiceClient historyServiceClient;
    private final NotificationServiceClient notificationServiceWebClient;
    private static final String PREFIX = "NX";

    public DepositServiceHandler(
            WalletServiceClient walletServiceClient,
            HistoryServiceClient historyServiceClient,
            NotificationServiceClient notificationServiceWebClient) {
        this.walletServiceClient = walletServiceClient;
        this.historyServiceClient = historyServiceClient;
        this.notificationServiceWebClient = notificationServiceWebClient;
    }

    @Override
    public ResponseEntity<?> createDeposit(DepositRequest request, String token) {
        try {
            WalletDTO wallet = walletServiceClient.findByUserId(request.getUserId(), token);

            if (!wallet.getUserId().equals(request.getUserId())) {
                return Error.createResponse(
                        "Fraudulent Attempt Detected",
                        HttpStatus.FORBIDDEN,
                        "The wallet does not belong to you.");
            }

            if (request.getDepositSystem() == null) {
                return Error.createResponse("Deposit system is required.", HttpStatus.BAD_REQUEST,
                        "Please provide a depositSystem: PAYSTACK, CARD, or USSD");
            }

            return switch (request.getDepositSystem()) {
                case PAYSTACK -> processPaystackDeposit(wallet, request, token);
                case CARD     -> processPaystackCardDeposit(wallet, request, token);
                case USSD     -> processPaystackUssdDeposit(wallet, request, token);
                default -> Error.createResponse("Unsupported deposit system.", HttpStatus.BAD_REQUEST,
                        "Use PAYSTACK, CARD, or USSD");
            };

        } catch (BanKDetailsNotFound e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage(), "details", e.getDetails()));
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Payment processing error", "details", "Failed to initialize payment."));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CARD
    // ─────────────────────────────────────────────────────────────────────────
    private ResponseEntity<?> processPaystackCardDeposit(WalletDTO wallet, DepositRequest request, String token)
            throws JsonProcessingException {

        BigDecimal previousBalance = resolveBalance(wallet, request.getCurrencyType().toString());
        boolean creditSuccess = creditWallet(request, token);

        if (!creditSuccess) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", "Failed to credit wallet. Transaction aborted."));
        }

        String transactionId = generateTransactionId();

        WalletDTO updated = walletServiceClient.findByUserId(request.getUserId(), token);
        BigDecimal newBalance = resolveBalance(updated, request.getCurrencyType().toString());

        DepositHistoryRequest historyRequest = buildHistoryRequest(request, previousBalance, newBalance, transactionId, "CARD");
        historyRequest.setDescription("DEPOSIT//INTO " + request.getUsername().toUpperCase()
                + " " + request.getCurrencyType() + " ACCOUNT");

        dispatchHistory(historyRequest, token);
        dispatchNotification(request, previousBalance, newBalance, transactionId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "status", "success",
                        "transactionId", transactionId,
                        "previousBalance", previousBalance,
                        "newBalance", newBalance,
                        "currency", request.getCurrencyType().toString(),
                        "depositMethod", "CARD",
                        "timestamp", System.currentTimeMillis()
                ));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // USSD
    // ─────────────────────────────────────────────────────────────────────────
    private ResponseEntity<?> processPaystackUssdDeposit(WalletDTO wallet, DepositRequest request, String token)
            throws JsonProcessingException {

        BigDecimal previousBalance = resolveBalance(wallet, request.getCurrencyType().toString());
        boolean creditSuccess = creditWallet(request, token);

        if (!creditSuccess) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", "Failed to credit wallet. Transaction aborted."));
        }

        String transactionId = generateTransactionId();

        WalletDTO updated = walletServiceClient.findByUserId(request.getUserId(), token);
        BigDecimal newBalance = resolveBalance(updated, request.getCurrencyType().toString());

        DepositHistoryRequest historyRequest = buildHistoryRequest(request, previousBalance, newBalance, transactionId, "USSD");
        historyRequest.setDescription("USSD//INTO " + request.getUsername().toUpperCase()
                + " " + request.getCurrencyType() + " ACCOUNT");

        dispatchHistory(historyRequest, token);
        dispatchNotification(request, previousBalance, newBalance, transactionId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "status", "success",
                        "transactionId", transactionId,
                        "previousBalance", previousBalance,
                        "newBalance", newBalance,
                        "currency", request.getCurrencyType().toString(),
                        "depositMethod", "USSD",
                        "timestamp", System.currentTimeMillis()
                ));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PAYSTACK (default / bank transfer)
    // ─────────────────────────────────────────────────────────────────────────
    private ResponseEntity<?> processPaystackDeposit(WalletDTO wallet, DepositRequest request, String token)
            throws JsonProcessingException {

        BigDecimal previousBalance = resolveBalance(wallet, request.getCurrencyType().toString());
        boolean creditSuccess = creditWallet(request, token);

        if (!creditSuccess) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", "Failed to credit wallet. Transaction aborted."));
        }

        String transactionId = generateTransactionId();

        BigDecimal newBalance = CompletableFuture.supplyAsync(() -> {
            WalletDTO updated = walletServiceClient.findByUserId(request.getUserId(), token);
            return resolveBalance(updated, request.getCurrencyType().toString());
        }).join();

        DepositHistoryRequest historyRequest = buildHistoryRequest(request, previousBalance, newBalance, transactionId, "PAYSTACK");

        dispatchHistory(historyRequest, token);
        dispatchNotification(request, previousBalance, newBalance, transactionId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "status", "success",
                        "transactionId", transactionId,
                        "previousBalance", previousBalance,
                        "newBalance", newBalance,
                        "currency", request.getCurrencyType().toString(),
                        "depositMethod", "PAYSTACK",
                        "timestamp", System.currentTimeMillis()
                ));
    }

    private DepositHistoryRequest buildHistoryRequest(
        DepositRequest request,
        BigDecimal previousBalance,
        BigDecimal newBalance,
        String transactionId,
        String channel) {

        String uName = request.getUsername().toUpperCase();
        String now = Instant.now().toString();

        DepositHistoryRequest h = new DepositHistoryRequest();

        // ── Core Identity ────────────────────────────────────────────
        h.setTransactionId(transactionId);
        h.setUserId(request.getUserId());
        h.setWalletId(request.getWalletId());
        h.setAccountHolder(uName);              // was setFullname()
        h.setSessionId(null);
        h.setReferenceId(null);
        h.setTerminalId(null);
        h.setErId(null);

        // ── Transaction Info ─────────────────────────────────────────
        h.setType(TransactionType.DEPOSIT);
        h.setDescription("DEPO//INTO " + uName + " " + request.getCurrencyType() + " ACCOUNT");
        h.setMessage("Deposited " + request.getAmount() + " into your " + request.getCurrencyType() + " wallet.");
        h.setCurrencyType(request.getCurrencyType().toString());
        h.setIpAddress(request.getIpAddress());
        h.setStatus("SUCCESS");
        h.setTimestamp(now);

        // ── Financial Amounts ────────────────────────────────────────
        h.setGrossAmount(request.getAmount());  // was setAmount()
        h.setFeeAmount(BigDecimal.ZERO);
        h.setTaxAmount(BigDecimal.ZERO);
        h.setNetAmount(request.getAmount());
        h.setPreviousBalance(previousBalance);
        h.setAvailableBalance(newBalance);      // was setNewBalance()
        h.setRunningBalance(newBalance);        // new — same value

        // ── Double-Entry Accounting ──────────────────────────────────
        h.setDebitCredit("CREDIT");
        h.setLedgerEntryType("DEPOSIT");

        // ── Counterparty (null for deposits) ─────────────────────────
        h.setCounterpartyWalletId(null);
        h.setCounterpartyUserId(null);
        h.setCounterpartyAccountHolder(null);
        h.setBankCode(null);
        h.setBankAccountNumber(null);
        h.setRoutingNumber(null);
        h.setExternalReference(null);

        // ── Multi-Currency ───────────────────────────────────────────
        h.setOriginalCurrency(request.getCurrencyType().toString());
        h.setExchangeRate(BigDecimal.ONE);

        // ── Reversal & Disputes ──────────────────────────────────────
        h.setParentHistoryId(null);             // null — no parent for fresh deposit
        h.setReversalReason(null);
        h.setDisputeStatus(null);
        h.setDisputeReference(null);

        // ── Idempotency & Retry ──────────────────────────────────────
        h.setIdempotencyKey(UUID.randomUUID().toString());
        h.setRetryCount(0);
        h.setFailureReason(null);
        h.setProcessedAt(now);

        // ── Channel & Device ─────────────────────────────────────────
        h.setChannel("Web");
        h.setDeviceId(request.getDeviceId());
        h.setUserAgent(request.getUserAgent());
        h.setGeoLocation(request.getGeoLocation());

        // ── Compliance & Risk ────────────────────────────────────────
        h.setRiskScore(BigDecimal.ZERO);
        h.setAmlFlag(false);
        h.setSanctionScreeningResult(null);
        h.setComplianceNote(null);
        h.setReviewedBy(null);

        // ── Admin Audit ──────────────────────────────────────────────
        h.setInitiatedBy(request.getUserId().toString());
        h.setApprovedBy(null);
        h.setApprovalTimestamp(null);
        h.setAdminNote("Deposit of " + request.getCurrencyType() + " " + request.getAmount() + " via " + channel);
        h.setManualAdjustmentFlag(false);

        // ── Metadata ─────────────────────────────────────────────────
        h.setCategory("DEPOSIT");
        h.setTags(null);

        return h;
    }
    // ─────────────────────────────────────────────────────────────────────────
    // Async helpers
    // ─────────────────────────────────────────────────────────────────────────
    private void dispatchHistory(DepositHistoryRequest historyRequest, String token) {
        CompletableFuture.runAsync(() -> {
            try { historyServiceClient.createDepositHistory(historyRequest, token); }
            catch (Exception ignored) {}
        });
    }

    private void dispatchNotification(DepositRequest request, BigDecimal previousBalance,
                                       BigDecimal newBalance, String transactionId) {
        CompletableFuture.runAsync(() -> {
            try {
                notificationServiceWebClient.sendDepositNotification(
                        request.getEmail(), request.getUsername(), request.getAmount(),
                        request.getCurrencyType(), previousBalance, newBalance,
                        request.getUsername().toUpperCase(), transactionId, request.getCurrencySymbol()
                );
            } catch (Exception ignored) {}
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Wallet helpers
    // ─────────────────────────────────────────────────────────────────────────
    private boolean creditWallet(DepositRequest request, String token) {
        try {
            Map<String, Object> result = walletServiceClient.creditUserWallet(
                    request.getUserId(),
                    request.getCurrencySymbol(),
                    request.getAmount().toString(),
                    request.getCurrencyType().toString(),
                    token
            ).get(5, TimeUnit.SECONDS);

            return Boolean.TRUE.equals(result.get("success"));

        } catch (TimeoutException | ExecutionException e) {
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private BigDecimal resolveBalance(WalletDTO wallet, String currencyCode) {
        if (wallet == null || wallet.getBalances() == null) return BigDecimal.ZERO;
        return wallet.getBalances().stream()
                .filter(b -> currencyCode.equalsIgnoreCase(b.getCurrencyCode()))
                .findFirst()
                .map(b -> new BigDecimal(b.getBalance().replace(",", "")))
                .orElse(BigDecimal.ZERO);
    }

    private String generateTransactionId() {
        long hash = Math.abs(UUID.randomUUID().getMostSignificantBits());
        return PREFIX + String.valueOf(hash).substring(0, 9);
    }
}