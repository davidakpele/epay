package pesco.example.authentication_service.clients;

import java.time.Duration;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pesco.example.authentication_service.dtos.AdminUserVerificationDTO;
import reactor.core.publisher.Mono;


@Service
public class AdminServiceClient {

    private final WebClient administratorServiceWebClient;

    public AdminServiceClient(WebClient administratorServiceWebClient) {
        this.administratorServiceWebClient = administratorServiceWebClient;
    }

    public boolean verifyUser(String username) {
        try {
            AdminUserVerificationDTO dto = new AdminUserVerificationDTO();
            dto.setUsername(username);
            
            Boolean result = administratorServiceWebClient.post()
                    .uri("/admin/api/verify-user")
                    .bodyValue(dto)
                    .retrieve()
                    .onStatus(status -> status.is5xxServerError(), response -> 
                        Mono.error(new RuntimeException("Admin service server error: " + response.statusCode()))
                    )
                    .onStatus(status -> status.is4xxClientError(), response -> 
                        Mono.error(new RuntimeException("Admin service client error: " + response.statusCode()))
                    )
                    .bodyToMono(Boolean.class)
                    .block(Duration.ofSeconds(30)); 
            
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify user with admin service: " + e.getMessage(), e);
        }
    }

    
}

