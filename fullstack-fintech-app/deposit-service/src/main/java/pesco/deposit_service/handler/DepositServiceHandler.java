package pesco.deposit_service.handler;

import java.math.BigDecimal;
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

            return processPaystackDeposit(wallet, request, token);

        } catch (BanKDetailsNotFound e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage(), "details", e.getDetails()));

        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Payment processing error", "details", "Failed to initialize payment."));
        }
    }


    private ResponseEntity<?> processPaystackDeposit(WalletDTO wallet, DepositRequest request, String token)
            throws JsonProcessingException {

        BigDecimal previousBalance = resolveBalance(wallet, request.getCurrencyType().toString());
        boolean creditSuccess = creditWallet(request, token);

        if (!creditSuccess) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", "Failed to credit wallet. Transaction aborted."));
        }

        String transactionId = generateTransactionId();

        CompletableFuture<BigDecimal> newBalanceFuture = CompletableFuture.supplyAsync(() -> {
            WalletDTO updated = walletServiceClient.findByUserId(request.getUserId(), token);
            return resolveBalance(updated, request.getCurrencyType().toString());
        });

        BigDecimal newBalance = newBalanceFuture.join();

        DepositHistoryRequest historyRequest = buildHistoryRequest(request, previousBalance, newBalance, transactionId);

        CompletableFuture.runAsync(() -> {
            try {
                historyServiceClient.createDepositHistory(historyRequest, token);
            } catch (Exception ignored) {}
        });

        CompletableFuture.runAsync(() -> {
            try {
                notificationServiceWebClient.sendDepositNotification(
                        request.getEmail(),
                        request.getUsername(),
                        request.getAmount(),
                        request.getCurrencyType(),
                        previousBalance,
                        newBalance,
                        request.getUsername().toUpperCase(),
                        transactionId,
                        request.getCurrencySymbol()
                );
            } catch (Exception ignored) {}
        });

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "status", "success",
                        "transactionId", transactionId,
                        "previousBalance", previousBalance,
                        "newBalance", newBalance,
                        "currency", request.getCurrencyType().toString(),
                        "timestamp", System.currentTimeMillis()
                ));
    }

    private boolean creditWallet(DepositRequest request, String token) {
        try {
            CompletableFuture<Map<String, Object>> future = walletServiceClient.creditUserWallet(
                    request.getUserId(),
                    request.getCurrencySymbol(),
                    request.getAmount().toString(),
                    request.getCurrencyType().toString(),
                    token
            );

            Map<String, Object> result = future.get(5, TimeUnit.SECONDS);
            return Boolean.TRUE.equals(result.get("success"));

        } catch (TimeoutException | ExecutionException e) {
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private DepositHistoryRequest buildHistoryRequest(
            DepositRequest request,
            BigDecimal previousBalance,
            BigDecimal newBalance,
            String transactionId) {

        String uName = request.getUsername().toUpperCase();

        DepositHistoryRequest h = new DepositHistoryRequest();
        h.setTransactionId(transactionId);
        h.setAmount(request.getAmount());
        h.setCurrencyType(request.getCurrencyType());
        h.setDescription("DEPO//INTO " + uName + " " + request.getCurrencyType() + " ACCOUNT");
        h.setType(TransactionType.DEPOSIT);
        h.setIp_address("");
        h.setMessage("Deposited " + request.getAmount() + " into your " + request.getCurrencyType() + " wallet.");
        h.setUserId(request.getUserId());
        h.setWalletId(request.getWalletId());
        h.setPreviousBalance(previousBalance);
        h.setNewBalance(newBalance);
        h.setFullname(uName);
        return h;
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