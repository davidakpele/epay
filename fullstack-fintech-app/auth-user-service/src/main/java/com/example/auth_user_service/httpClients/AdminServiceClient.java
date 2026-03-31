package com.example.auth_user_service.httpClients;

import java.time.Duration;
import org.springframework.web.reactive.function.client.WebClient;
import com.example.auth_user_service.dtos.AdminUserVerificationDTO;
import com.example.auth_user_service.interfaces.IAdminServiceClient;
import com.example.auth_user_service.responses.AdminUserVerificationResponse;
import reactor.core.publisher.Mono;

public class AdminServiceClient implements IAdminServiceClient{

    private final WebClient administratorServiceWebClient;

    public AdminServiceClient(WebClient administratorServiceWebClient) {
        this.administratorServiceWebClient = administratorServiceWebClient;
    }

    @Override
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
