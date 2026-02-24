package com.pesco.wallet_service.client;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Service
public class NotificationServiceClient {

    private final WebClient notificationServiceWebClient;
    private final String baseUrl;

    public NotificationServiceClient(
            WebClient notificationServiceWebClient,
            @Value("${notification-service.base-url:}") String baseUrl // Default to empty if not found
    ) {
        this.notificationServiceWebClient = notificationServiceWebClient;
        this.baseUrl = baseUrl;
    }

    public boolean sendSwapAlert(String email, String username, BigDecimal amount, String currency,
                                 BigDecimal availableBalance, String transactionId, BigDecimal previousBalance,String swapCurrency) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("email", email);
            requestBody.put("accountHolder", username);
            requestBody.put("amount", amount);
            requestBody.put("currencySymbol", getCurrencySymbol(currency));
            requestBody.put("availableBalance", availableBalance);
            requestBody.put("previousBalance", previousBalance);
            requestBody.put("currencyExchange", swapCurrency);
            requestBody.put("transactionId", transactionId);

            String endpoint = "/send/swap-wallet-message";
            String fullUrl = (baseUrl != null ? baseUrl : "BASE_URL_NOT_SET") + endpoint;
            boolean success = Boolean.TRUE.equals(notificationServiceWebClient.post()
                    .uri(endpoint)
                    .bodyValue(requestBody)
                    .exchangeToMono(response -> {
                        if (response.statusCode().is2xxSuccessful()) {
                            System.out.println("Swap alert sent successfully.");
                            System.out.println("HTTP Status: " + response.statusCode());
                            return Mono.just(true);
                        } else {
                            System.err.println("Failed to send swap alert.");
                            System.err.println("HTTP Status: " + response.statusCode());
                            response.bodyToMono(String.class)
                                    .doOnNext(body -> System.err.println("Response Body: " + body))
                                    .subscribe();
                            return Mono.just(false);
                        }
                    })
                    .block());

            if (success) {
                System.out.println("Swap notification successfully delivered to Notification Service.");
            } else {
                System.err.println("Swap notification failed to deliver.");
            }

            return success;

        } catch (Exception ex) {
            System.err.println("Error sending swap notification: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    private String getCurrencySymbol(String currencyCode) {
        Map<String, String> currencySymbols = Map.of(
                "USD", "$", "EUR", "€", "GBP", "£", "JPY", "¥",
                "AUD", "A$", "CAD", "C$", "CHF", "CHF", "CNY", "¥",
                "INR", "₹", "NGN", "₦"
        );
        return currencySymbols.getOrDefault(currencyCode, "$");
    }
}
