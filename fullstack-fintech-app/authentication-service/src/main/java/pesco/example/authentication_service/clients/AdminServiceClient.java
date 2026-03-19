package pesco.example.authentication_service.clients;

import java.time.Duration;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pesco.example.authentication_service.dtos.AdminUserVerificationDTO;
import pesco.example.authentication_service.responses.AdminUserVerificationResponse;
import reactor.core.publisher.Mono;


@Service
public class AdminServiceClient {

    private final WebClient administratorServiceWebClient;

    public AdminServiceClient(WebClient administratorServiceWebClient) {
        this.administratorServiceWebClient = administratorServiceWebClient;
    }

    public boolean verifyUser(String username, String token) {
        try {

            AdminUserVerificationDTO dto = new AdminUserVerificationDTO();
            dto.setUsername(username);

            AdminUserVerificationResponse result = administratorServiceWebClient.post()
                    .uri("/admin/api/verify-user")
                    .header("Authorization", token)
                    .bodyValue(dto)
                    .retrieve()
                    .onStatus(status -> status.is5xxServerError(),
                            response -> Mono.error(new RuntimeException(
                                    "Admin service server error: " + response.statusCode())))
                    .onStatus(status -> status.is4xxClientError(),
                            response -> Mono.error(new RuntimeException(
                                    "Admin service client error: " + response.statusCode())))
                    .bodyToMono(AdminUserVerificationResponse.class)
                    .block(Duration.ofSeconds(30));

            return result != null && result.isVerified();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to verify user with admin service: " + e.getMessage(), e);
        }
    }

    
}

