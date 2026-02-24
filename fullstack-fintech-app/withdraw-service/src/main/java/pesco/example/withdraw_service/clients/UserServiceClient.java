package pesco.example.withdraw_service.clients;

import java.util.Map;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import pesco.example.withdraw_service.dtos.UserDTO;
import pesco.example.withdraw_service.enums.BanActions;
import pesco.example.withdraw_service.exceptions.UserClientNotFoundException;
import reactor.core.publisher.Mono;


@Service
public class UserServiceClient {

    private final WebClient authServiceWebClient;

    public UserServiceClient(WebClient authServiceWebClient) {
        this.authServiceWebClient = authServiceWebClient;
    }

    public UserDTO findById(Long id, String token) {
        return this.authServiceWebClient.get()
                .uri("/user/userId/{userId}", id)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorMessage -> {
                                    if (clientResponse.statusCode().is4xxClientError()) {
                                        String details = extractDetailsFromError(errorMessage);
                                        return Mono.error(
                                                new UserClientNotFoundException("User not found: " + details,
                                                        errorMessage));
                                    }
                                    return Mono.error(new RuntimeException("Server error: " + errorMessage));
                                }))
                .bodyToMono(String.class)
                .flatMap(response -> {
                    // Convert the raw response to UserDTO
                    UserDTO userDTO = convertToUserDTO(response);
                    return Mono.just(userDTO);
                })
                .switchIfEmpty(Mono.error(new UserClientNotFoundException("User not found: No data returned", token)))
                .block();

    }

    public UserDTO authenticateUser(String username, String token) {
        return this.authServiceWebClient.get()
                .uri("/user/username/{username}", username)
                .headers(headers -> headers.setBearerAuth(token))
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorMessage -> {
                                    if (clientResponse.statusCode().is4xxClientError()) {
                                        String details = extractDetailsFromError(errorMessage);
                                        return Mono.error(new UserClientNotFoundException("User not found", details));
                                    }
                                    return Mono.error(new RuntimeException("Server error"));
                                }))
                .bodyToMono(UserDTO.class)
                .block();
    }

    public UserDTO getUserByUsername(String username) {
        return this.authServiceWebClient.get()
                .uri("/user/username/{username}", username)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorMessage -> {
                                    if (clientResponse.statusCode().is4xxClientError()) {
                                        String details = extractDetailsFromError(errorMessage);
                                        return Mono.error(new UserClientNotFoundException("User not found", details));
                                    }
                                    return Mono.error(new RuntimeException("Server error"));
                                }))
                .bodyToMono(UserDTO.class)
                .block();
    }

    private String extractDetailsFromError(String errorBody) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(errorBody);

            if (node.has("details")) {
                return node.get("details").asText();
            } else if (node.has("message")) {
                return node.get("message").asText();
            } else {
                return "Unknown error occurred";
            }
        } catch (Exception e) {
            return "Failed to parse error details";
        }
    }

    public UserDTO fetchById(Long userId) {
        return this.authServiceWebClient.get()
                .uri("/user/userId/{userId}", userId)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorMessage -> {
                                    if (clientResponse.statusCode().is4xxClientError()) {
                                        String details = extractDetailsFromError(errorMessage);
                                        return Mono.error(new UserClientNotFoundException("User not found", details));
                                    }
                                    return Mono.error(new RuntimeException("Server error"));
                                }))
                .bodyToMono(UserDTO.class)
                .block();
    }

    public UserDTO convertToUserDTO(String response) {
        try {
            // Create an ObjectMapper instance
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            // Convert the raw JSON response to a UserDTO object
            return objectMapper.readValue(response, UserDTO.class);
        } catch (Exception e) {
            // Log the error details for better debugging
            System.err.println("Error converting response to UserDTO: " + e.getMessage());
            e.printStackTrace();

            // Rethrow the exception with additional context
            throw new RuntimeException("Error converting response to UserDTO", e);
        }
    }

    public void updateUserAccountStatus(Long id, BanActions suspiciousActivity, String token) {
        try {
            // Send the PUT request
            this.authServiceWebClient.put()
                    .uri("/user/settings/deactivate-account/{id}", id)
                    .header("Authorization", "Bearer " + token)
                    .bodyValue(suspiciousActivity)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(errorMessage -> {
                                        if (clientResponse.statusCode().is4xxClientError()) {
                                            String details = extractDetailsFromError(errorMessage);
                                            return Mono
                                                    .error(new UserClientNotFoundException("User not found", details));
                                        }
                                        return Mono.error(new RuntimeException("Server error"));
                                    }))
                    .toBodilessEntity()
                    .block();
        } catch (Exception ex) {
            System.err.println("Error updating user account status: " + ex.getMessage());
        }
    }

    public void blockUserAccount(Long id, String token) {
        this.authServiceWebClient.put()
                .uri("/user/block/userId/{id}", id)
                .header("Authorization", "Bearer " + token)
                .bodyValue(Map.of("blocked", true))
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class)
                        .flatMap(errorBody -> {
                            System.err.println("Error Response: " + errorBody);
                            return Mono.error(new RuntimeException("User account blocking failed: " + errorBody));
                        }))
                .bodyToMono(String.class)
                .block();
    }

    public UserDTO findByUsername(String username, String token) {
        return this.authServiceWebClient.get()
                .uri("/user/username/{username}", username)
                .headers(headers -> headers.setBearerAuth(token))
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorMessage -> {
                                    if (clientResponse.statusCode().is4xxClientError()) {
                                        String details = extractDetailsFromError(errorMessage);
                                        return Mono.error(new UserClientNotFoundException("The User `"+username+"` not found.", details));
                                    }
                                    return Mono.error(new RuntimeException("Server error"));
                                }))
                .bodyToMono(UserDTO.class)
                .block();
    }
}
