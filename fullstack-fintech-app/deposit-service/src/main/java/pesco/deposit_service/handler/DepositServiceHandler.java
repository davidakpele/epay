package pesco.deposit_service.handler;

import java.math.BigDecimal;
import java.util.HashMap;
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
import pesco.deposit_service.clients.BankListServiceClient;
import pesco.deposit_service.clients.HistoryServiceClient;
import pesco.deposit_service.clients.NotificationServiceClient;
import pesco.deposit_service.clients.PayStackServiceClient;
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
    private final BankListServiceClient bankListServiceClient;
    private final PayStackServiceClient payStackServiceClient;
    private final HistoryServiceClient historyServiceClient;
    private final NotificationServiceClient notificationServiceWebClient;
    private static final String PREFIX = "NX";

    public DepositServiceHandler(WalletServiceClient walletServiceClient, BankListServiceClient bankListServiceClient, 
        PayStackServiceClient payStackServiceClient, HistoryServiceClient historyServiceClient,
        NotificationServiceClient notificationServiceWebClient) {
        this.walletServiceClient = walletServiceClient;
        this.bankListServiceClient = bankListServiceClient;
        this.payStackServiceClient = payStackServiceClient;
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
        Map<String, Object> response = new HashMap<>();
        BigDecimal recipientPreviousBalance = wallet.getBalances().stream()
            .filter(b -> request.getCurrencyType().toString().equalsIgnoreCase(b.getCurrencyCode()))
            .findFirst()
            .map(b -> new BigDecimal(b.getBalance().replace(",", "")))
            .orElse(BigDecimal.ZERO);

        CompletableFuture<Boolean> creditWallet = CompletableFuture.supplyAsync(() -> {
            try {
                CompletableFuture<Map<String, Object>> result = walletServiceClient.creditUserWallet(
                    request.getUserId(), 
                    request.getCurrencySymbol(), 
                    request.getAmount().toString(), 
                    request.getCurrencyType().toString(),
                    token
                );
                
                Map<String, Object> responses = result.get(5, TimeUnit.SECONDS);
                
                if (!Boolean.TRUE.equals(responses.get("success"))) {
                    System.err.println("Wallet credit failed: " + responses.get("message"));
                    return false; 
                }
                return true; 
                
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                System.err.println("Error crediting wallet: " + e.getMessage());
                e.printStackTrace();
                return false; 
            }
        });
        
        Boolean creditSuccess = creditWallet.join();
        
        if (!Boolean.TRUE.equals(creditSuccess)) {
            response.put("status", "error");
            response.put("message", "Failed to credit wallet. Transaction aborted.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        String transactionId = generateTransactionId();
        WalletDTO reBalanace = walletServiceClient.findByUserId(request.getUserId(), token);

        BigDecimal recipientNewBalance = reBalanace.getBalances().stream()
            .filter(b -> request.getCurrencyType().toString().equalsIgnoreCase(b.getCurrencyCode()))
            .findFirst()
            .map(b -> new BigDecimal(b.getBalance().replace(",", ""))) 
            .orElse(BigDecimal.ZERO);
            
        String u_name = request.getUsername().toUpperCase();
        DepositHistoryRequest depositHistoryRequest = new DepositHistoryRequest();
        depositHistoryRequest.setAmount(request.getAmount());
        depositHistoryRequest.setCurrencyType(request.getCurrencyType());
        depositHistoryRequest.setDescription("DEPO//INTO "+u_name+" "+request.getCurrencyType()+" ACCOUNT");
        depositHistoryRequest.setType(TransactionType.DEPOSIT);
        depositHistoryRequest.setIp_address("");
        depositHistoryRequest.setMessage("Deposited " +request.getAmount()+" into your "+request.getCurrencyType()+" wallet.");
        depositHistoryRequest.setUserId(request.getUserId());
        depositHistoryRequest.setWalletId(request.getWalletId());
        depositHistoryRequest.setPreviousBalance(recipientPreviousBalance);
        depositHistoryRequest.setNewBalance(recipientNewBalance);
        depositHistoryRequest.setFullname(u_name);
        depositHistoryRequest.setTransactionId(transactionId);

        CompletableFuture.runAsync(() -> historyServiceClient.createDepositHistory(depositHistoryRequest, token)).join();
        
        CompletableFuture.runAsync(() -> notificationServiceWebClient.sendDepositNotification(
            email, 
            username, 
            request.getAmount(),
            request.getCurrencyType(),
            recipientPreviousBalance, 
            recipientNewBalance,
            u_name,
            transactionId,
            request.getCurrencySymbol()
            ))
        .join();
        
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
