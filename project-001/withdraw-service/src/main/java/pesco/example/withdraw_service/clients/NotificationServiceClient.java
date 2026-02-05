package pesco.example.withdraw_service.clients;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;
 
@Service
public class NotificationServiceClient {
 
    private final WebClient notificationServiceWebClient;

    public NotificationServiceClient(WebClient notificationServiceWebClient) {
        this.notificationServiceWebClient = notificationServiceWebClient;
    }

    public Object sendDebitAlert(String senderEmail, String senderUsername, String recipientUsername, BigDecimal amount, String currency, BigDecimal feeAmount, BigDecimal senderNewWalletBalance, String transactionId, BigDecimal previousBalance) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("senderEmail", senderEmail);
            requestBody.put("senderFullName", senderUsername);
            requestBody.put("receiverFullName", recipientUsername);
            requestBody.put("transferAmount", amount);
            requestBody.put("currency", currency);
            requestBody.put("feeAmount", feeAmount);
            requestBody.put("transactionId", transactionId);
            requestBody.put("balance", senderNewWalletBalance);
            requestBody.put("previousBalance", previousBalance);
            System.out.println("Debit payload: "+requestBody);
            notificationServiceWebClient.post()
                    .uri("/send/debit-wallet-message")
                    .bodyValue(requestBody)
                    .exchangeToMono(response -> {
                        if (response.statusCode().is2xxSuccessful()) {
                            return Mono.just(true);
                        } else {
                            return Mono.just(false);
                        }
                    })
                    .block();
                return null;
        } catch (Exception ex) {
            System.err.println("Error sending transaction notifications: " + ex.getMessage());
            return null;
        }
    }

    public boolean sendCreditAlert( String email, String username, String recipientUsername, BigDecimal amount, String currency, BigDecimal recieverNewWalletBalance, String transactionId, BigDecimal previousBalance) {
        try {
            // Create the request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("recipientEmail", email);
            requestBody.put("senderFullName", username);
            requestBody.put("receiverFullName", recipientUsername);
            requestBody.put("transferAmount", amount);
            requestBody.put("currency", currency);
            requestBody.put("transactionId", transactionId);
            requestBody.put("recipientTotalBalance", recieverNewWalletBalance);
            requestBody.put("previousBalance", previousBalance);
            notificationServiceWebClient.post()
                    .uri("/send/credit-wallet-message")
                    .bodyValue(requestBody)
                    .exchangeToMono(response -> {
                        if (response.statusCode().is2xxSuccessful()) {
                            return Mono.just(true);
                        } else {
                            return Mono.just(false);
                        }
                    })
                    .block();       
            return true;
        } catch (Exception ex) {
            // Handle exceptions
            System.err.println("Error sending transaction notifications: " + ex.getMessage());
            return false;
        }
    }

    public boolean blockUserWalletNotification(String userEmail, String userFirstname, String userLastname, String reason) {
        try {
            // Create the request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("email", userEmail);
            requestBody.put("firstName", userFirstname); 
            requestBody.put("lastName", userLastname);   
            requestBody.put("message", reason);         
            notificationServiceWebClient.post()
                    .uri("/notification/block-wallet-notification")
                    .bodyValue(requestBody)
                    .exchangeToMono(response -> {
                        if (response.statusCode().is2xxSuccessful()) {
                            return Mono.just(true);
                        } else {
                            return Mono.just(false);
                        }
                    })
                    .block();       
            return true;
        } catch (Exception ex) {
            System.err.println("Error sending transaction notifications: " + ex.getMessage());
            return false;
        }
    }
}
