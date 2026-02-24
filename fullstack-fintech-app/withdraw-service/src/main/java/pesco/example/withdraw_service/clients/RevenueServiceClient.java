package pesco.example.withdraw_service.clients;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class RevenueServiceClient {

    private final WebClient revenueServiceWebClient;

    public RevenueServiceClient(WebClient revenueServiceWebClient) {
        this.revenueServiceWebClient = revenueServiceWebClient;
    }

    public Object creditPlatformRevenue(BigDecimal feeAmount, String currencyType) {
        try {
            // Create the request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("amount", feeAmount);
            requestBody.put("currency", currencyType);
            requestBody.put("transactionType", "CREDITED");
            // Send the POST request
            return revenueServiceWebClient.post()
                    .uri("/api/revenue/transactions") 
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Object.class) 
                    .block();
        } catch (Exception ex) {
            // Handle exceptions
            System.err.println("Error adding to platform revenue: " + ex.getMessage());
            return null; 
        }
    }

}