package pesco.deposit_service.handler;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    
    private static final Logger log = LoggerFactory.getLogger(DepositServiceHandler.class);
    private final WalletServiceClient walletServiceClient;
    private final HistoryServiceClient historyServiceClient;
    private final NotificationServiceClient notificationServiceWebClient;
    private static final String PREFIX = "NX";

    public DepositServiceHandler(WalletServiceClient walletServiceClient, HistoryServiceClient historyServiceClient, NotificationServiceClient notificationServiceWebClient) {
        this.walletServiceClient = walletServiceClient;
        this.historyServiceClient = historyServiceClient;
        this.notificationServiceWebClient = notificationServiceWebClient;
    }
    
    @Override
    public ResponseEntity<?> createDeposit(DepositRequest request, String token) {
        Map<String, Object> response = new HashMap<>();

        try {
            // BankListDTO bankDetails = bankListServiceClient.findByAccountNumber(request.getAccountNumber(), token);

            WalletDTO wallet = walletServiceClient.findByUserId(request.getUserId(), token);
            if (!wallet.getUserId().equals(request.getUserId())) {
                return Error.createResponse(
                        "Fraudulent Attempt Detected",
                        HttpStatus.FORBIDDEN,
                        "The wallet does not belong to you.");
            }
            return processPaystackDeposit(wallet, request, request.getEmail(), request.getUserId(), request.getUsername(), wallet.getUserId(), token);
        } catch (BanKDetailsNotFound e) {
            response.put("message", e.getMessage());
            response.put("details", e.getDetails());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (JsonProcessingException e) {
            response.put("message", "Payment processing error");
            response.put("details", "Failed to initialize payment.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);

        }
    }

    private ResponseEntity<?> processPaystackDeposit(WalletDTO wallet, DepositRequest request, String email, Long userId, String username, Long walletId, String token)
        throws JsonProcessingException {

        log.info("[processPaystackDeposit] - START | userId: {} | walletId: {} | amount: {} | currency: {}",
                userId, walletId, request.getAmount(), request.getCurrencyType());

        Map<String, Object> response = new HashMap<>();
        long totalStart = System.currentTimeMillis();

        log.debug("[processPaystackDeposit] Resolving previous balance for currency: {}", request.getCurrencyType());
        BigDecimal recipientPreviousBalance = wallet.getBalances().stream()
                .filter(b -> request.getCurrencyType().toString().equalsIgnoreCase(b.getCurrencyCode()))
                .findFirst()
                .map(b -> new BigDecimal(b.getBalance().replace(",", "")))
                .orElse(BigDecimal.ZERO);
        log.info("[processPaystackDeposit] Previous balance resolved: {} {}", recipientPreviousBalance, request.getCurrencyType());

        log.info("[processPaystackDeposit] - Attempting to credit wallet | userId: {} | amount: {} | symbol: {}",
                request.getUserId(), request.getAmount(), request.getCurrencySymbol());

        long creditStart = System.currentTimeMillis();
        CompletableFuture<Boolean> creditWallet = CompletableFuture.supplyAsync(() -> {
            log.debug("[processPaystackDeposit] [async-credit] Thread: {} | Calling walletServiceClient.creditUserWallet...",
                    Thread.currentThread().getName());
            try {
                CompletableFuture<Map<String, Object>> result = walletServiceClient.creditUserWallet(
                        request.getUserId(),
                        request.getCurrencySymbol(),
                        request.getAmount().toString(),
                        request.getCurrencyType().toString(),
                        token
                );

                Map<String, Object> responses = result.get(5, TimeUnit.SECONDS);
                log.debug("[processPaystackDeposit] [async-credit] Raw wallet response: {}", responses);

                if (!Boolean.TRUE.equals(responses.get("success"))) {
                    log.error("[processPaystackDeposit] [async-credit] - Wallet credit failed | message: {}", responses.get("message"));
                    return false;
                }

                log.info("[processPaystackDeposit] [async-credit] - Wallet credited successfully.");
                return true;

            } catch (TimeoutException e) {
                log.error("[processPaystackDeposit] [async-credit] - Timeout after 5s waiting for walletServiceClient.");
                return false;
            } catch (InterruptedException e) {
                log.error("[processPaystackDeposit] [async-credit] - Thread interrupted: {}", e.getMessage());
                Thread.currentThread().interrupt();
                return false;
            } catch (ExecutionException e) {
                log.error("[processPaystackDeposit] [async-credit] - ExecutionException: {} | Cause: {}",
                        e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "unknown");
                return false;
            }
        });

        Boolean creditSuccess = creditWallet.join();
        log.info("[processPaystackDeposit] - Credit wallet result: {} | Elapsed: {}ms",
                creditSuccess, System.currentTimeMillis() - creditStart);

        if (!Boolean.TRUE.equals(creditSuccess)) {
            log.error("[processPaystackDeposit] - Wallet credit failed — aborting deposit | userId: {} | amount: {}",
                    userId, request.getAmount());
            response.put("status", "error");
            response.put("message", "Failed to credit wallet. Transaction aborted.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        String transactionId = generateTransactionId();
        log.info("[processPaystackDeposit] - Generated transactionId: {}", transactionId);

        log.info("[processPaystackDeposit] - Fetching updated wallet balance for userId: {}", request.getUserId());
        long balanceFetchStart = System.currentTimeMillis();

        WalletDTO reBalance = walletServiceClient.findByUserId(request.getUserId(), token);
        log.debug("[processPaystackDeposit] Wallet refetch completed in {}ms", System.currentTimeMillis() - balanceFetchStart);

        BigDecimal recipientNewBalance = reBalance.getBalances().stream()
                .filter(b -> request.getCurrencyType().toString().equalsIgnoreCase(b.getCurrencyCode()))
                .findFirst()
                .map(b -> new BigDecimal(b.getBalance().replace(",", "")))
                .orElse(BigDecimal.ZERO);

        log.info("[processPaystackDeposit] - Balance snapshot | Previous: {} | New: {} | Diff: {} | Currency: {}",
                recipientPreviousBalance, recipientNewBalance,
                recipientNewBalance.subtract(recipientPreviousBalance),
                request.getCurrencyType());

        String uName = request.getUsername().toUpperCase();
        log.debug("[processPaystackDeposit] Building DepositHistoryRequest | user: {} | txId: {}", uName, transactionId);

        DepositHistoryRequest depositHistoryRequest = new DepositHistoryRequest();
        depositHistoryRequest.setAmount(request.getAmount());
        depositHistoryRequest.setCurrencyType(request.getCurrencyType());
        depositHistoryRequest.setDescription("DEPO//INTO " + uName + " " + request.getCurrencyType() + " ACCOUNT");
        depositHistoryRequest.setType(TransactionType.DEPOSIT);
        depositHistoryRequest.setIp_address("");
        depositHistoryRequest.setMessage("Deposited " + request.getAmount() + " into your " + request.getCurrencyType() + " wallet.");
        depositHistoryRequest.setUserId(request.getUserId());
        depositHistoryRequest.setWalletId(request.getWalletId());
        depositHistoryRequest.setPreviousBalance(recipientPreviousBalance);
        depositHistoryRequest.setNewBalance(recipientNewBalance);
        depositHistoryRequest.setFullname(uName);
        depositHistoryRequest.setTransactionId(transactionId);

        log.debug("[processPaystackDeposit] DepositHistoryRequest built: amount={}, currency={}, walletId={}, txId={}",
                depositHistoryRequest.getAmount(), depositHistoryRequest.getCurrencyType(),
                depositHistoryRequest.getWalletId(), depositHistoryRequest.getTransactionId());

        // ─── Step 6: Save History ──────────────────────────────────────────────────
        log.info("[processPaystackDeposit] - Persisting deposit history | txId: {}", transactionId);
    
        CompletableFuture.runAsync(() -> {
            log.debug("[processPaystackDeposit] [async-history] Thread: {} | Calling historyServiceClient...",
                    Thread.currentThread().getName());
            try {
                historyServiceClient.createDepositHistory(depositHistoryRequest, token);
                log.info("[processPaystackDeposit] [async-history] - History saved | txId: {}", transactionId);
            } catch (Exception e) {
                log.error("[processPaystackDeposit] [async-history] - Failed to save history | txId: {} | error: {}",
                        transactionId, e.getMessage(), e);
            }
        }).join();

        long notifStart = System.currentTimeMillis();

        CompletableFuture.runAsync(() -> {
            log.debug("[processPaystackDeposit] [async-notification] Thread: {} | Calling notificationServiceWebClient...",
                    Thread.currentThread().getName());
            try {
                notificationServiceWebClient.sendDepositNotification(
                        email, username, request.getAmount(), request.getCurrencyType(),
                        recipientPreviousBalance, recipientNewBalance,
                        uName, transactionId, request.getCurrencySymbol()
                );
                log.info("[processPaystackDeposit] [async-notification] - Notification sent | txId: {}", transactionId);
            } catch (Exception e) {
                log.error("[processPaystackDeposit] [async-notification] - Notification failed | txId: {} | error: {}",
                        transactionId, e.getMessage(), e);
            }
        }).join();

        log.info("[processPaystackDeposit] - Notification step completed in {}ms", System.currentTimeMillis() - notifStart);

        // ─── Step 8: Final Response ────────────────────────────────────────────────
        long totalElapsed = System.currentTimeMillis() - totalStart;
        log.info("[processPaystackDeposit] - COMPLETE | txId: {} | userId: {} | totalDuration: {}ms",
                transactionId, userId, totalElapsed);

        response.put("status", "success");
        response.put("details", "Please complete your transaction.");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
   
    private String generateTransactionId() {
        long hash = Math.abs(UUID.randomUUID().getMostSignificantBits());
        String digits = String.valueOf(hash).substring(0, 9);
        return PREFIX + digits;
    }
  
    

}
