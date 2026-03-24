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
        historyRequest.setDescription("CARD//INTO " + request.getUsername().toUpperCase()
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

    // ─────────────────────────────────────────────────────────────────────────
    // Core builder — all fields mapped to C# History model
    // ─────────────────────────────────────────────────────────────────────────
    private DepositHistoryRequest buildHistoryRequest(
            DepositRequest request,
            BigDecimal previousBalance,
            BigDecimal newBalance,
            String transactionId,
            String channel) {

        String uName = request.getUsername().toUpperCase();

        DepositHistoryRequest h = new DepositHistoryRequest();

        // ── Core Identity ────────────────────────────────────────────
        h.setTransactionId(transactionId);
        h.setUserId(request.getUserId());
        h.setWalletId(request.getWalletId());
        h.setFullname(uName);                           // → AccountHolder

        // ── Transaction Info ─────────────────────────────────────────
        h.setType(TransactionType.DEPOSIT);
        h.setDescription("DEPO//INTO " + uName + " " + request.getCurrencyType() + " ACCOUNT");
        h.setMessage("Deposited " + request.getAmount() + " into your " + request.getCurrencyType() + " wallet.");
        h.setCurrencyType(request.getCurrencyType());
        h.setIpAddress(request.getIpAddress());                              // populate from HttpServletRequest if available

        // ── Financial Amounts ────────────────────────────────────────
        h.setAmount(request.getAmount());               // → GrossAmount
        h.setFeeAmount(BigDecimal.ZERO);                // → FeeAmount   (extend later if Paystack charges fees)
        h.setTaxAmount(BigDecimal.ZERO);                // → TaxAmount
        h.setNetAmount(request.getAmount());            // → NetAmount   (gross - fee - tax)
        h.setPreviousBalance(previousBalance);          // → PreviousBalance
        h.setNewBalance(newBalance);                    // → AvailableBalance + RunningBalance

        // ── Double-Entry Accounting ──────────────────────────────────
        h.setDebitCredit("CREDIT");                     // deposits are always credits
        h.setLedgerEntryType("DEPOSIT");                // → LedgerEntryType

        // ── Channel & Device ─────────────────────────────────────────
        h.setChannel(channel);                          // PAYSTACK | CARD | USSD → TransactionChannel
        h.setDeviceId(request.getDeviceId());           // add getDeviceId() to DepositRequest if absent
        h.setUserAgent(request.getUserAgent());         // add getUserAgent() to DepositRequest if absent
        h.setGeoLocation(request.getGeoLocation());     // add getGeoLocation() to DepositRequest if absent
        h.setInitiatedBy(request.getUserId().toString());
        h.setStatus("SUCCESS");
        h.setProcessedAt(Instant.now().toEpochMilli());        
        // ── Idempotency ──────────────────────────────────────────────
        h.setIdempotencyKey(request.getUserId() + "_" + transactionId);   
        // ── Multi-Currency ───────────────────────────────────────────
        h.setOriginalCurrency(request.getCurrencyType().toString());
        h.setExchangeRate(BigDecimal.ONE);              // 1.0 unless FX conversion is involved

        // ── Metadata ─────────────────────────────────────────────────
        h.setCategory("DEPOSIT");                       // → TransactionCategory

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