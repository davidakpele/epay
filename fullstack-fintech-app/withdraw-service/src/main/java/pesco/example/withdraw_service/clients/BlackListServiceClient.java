package pesco.example.withdraw_service.clients;

import java.util.Map;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pesco.example.withdraw_service.enums.BanActions;
import reactor.core.publisher.Mono;

@Service
public class BlackListServiceClient {

    private final WebClient blacklistServiceWebClient;

    public BlackListServiceClient(WebClient blacklistServiceWebClient) {
        this.blacklistServiceWebClient = blacklistServiceWebClient;
    }
    
    public Boolean FindByWalletId(Long id, String token) {
        try {
            return blacklistServiceWebClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/blacklist/status/{id}").build(id))
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
        } catch (Exception ex) {
            System.err.println("Error finding wallet by ID: " + ex.getMessage());
            return false;
        }
    }
    
    public void blockUserWallet(Long walletId, BanActions fraudulentActivity, String token) {
        Map<String, Object> requestBody = Map.of(
            "wallet_id", walletId,
            "bank_banned_reason", fraudulentActivity,
            "is_block", true
        );

        this.blacklistServiceWebClient.post()
                .uri("/blacklist/add")
                .header("Authorization", "Bearer " + token)
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class)
                        .flatMap(errorBody -> {
                            System.err.println("Error Response: " + errorBody);
                            return Mono.error(new RuntimeException("Wallet block failed: " + errorBody));
                        }))
                .bodyToMono(String.class)
                .block();
    }


}
