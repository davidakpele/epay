package com.pesco.wallet_service.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pesco.wallet_service.bootstrap.UsersDetailsDTO;
import com.pesco.wallet_service.dtos.UserDTO;
import com.pesco.wallet_service.enums.BannedReasons;
import com.pesco.wallet_service.exceptions.UserClientNotFoundException;
import reactor.core.publisher.Mono;

@Service
public class UserServiceClient {

    private final WebClient webClient;
  
    public UserServiceClient(@Qualifier("authServiceWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public UserDTO getUserById(Long id, String token) {
        return this.webClient.get()
                .uri("/user/userId/{userId}", id)
                .headers(headers -> headers.setBearerAuth(token))
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

    public UserDTO findUserByUsernameInPublicRoute(String username) {
        return this.webClient.get()
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

    public boolean UpdateUserTranferPinInUserRecord(String token, Long id, boolean updateUserRecordTransferPin) {
        try {
            this.webClient.put()
                    .uri("/user/{id}/updateUserRecord/transferPin/status", id)
                    .headers(headers -> headers.setBearerAuth(token))
                    .bodyValue(updateUserRecordTransferPin)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            response -> {
                                return response.bodyToMono(String.class)
                                        .flatMap(body -> {
                                            throw new RuntimeException("Error occurred: " + body);
                                        });
                            })
                    .toBodilessEntity()
                    .block();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String extractDetailsFromError(String errorMessage) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(errorMessage);
            return rootNode.path("message").asText();
        } catch (JsonProcessingException e) {
            return "No details available";
        }
    }

    public UserDTO fetchPublicUserById(Long userId, String token) {
        return this.webClient.get()
                .uri("/user/{userId}", userId)
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

    public UserDTO convertToUserDTO(String response) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            return objectMapper.readValue(response, UserDTO.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException("Error converting response to UserDTO", e);
        }
    }

    public void updateUserAccountStatus(Long id, BannedReasons suspiciousActivity, String token) {
        try {
            this.webClient.put()
                    .uri("/user/{id}/account/lock", id)
                    .headers(headers -> headers.setBearerAuth(token))
                    .bodyValue(suspiciousActivity)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            response -> {
                                return response.bodyToMono(String.class)
                                        .flatMap(body -> {
                                            throw new RuntimeException("Error occurred: " + body);
                                        });
                            })
                    .toBodilessEntity()
                    .block();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public UserDTO findById(Long userId) {
        String endpoint = "/user/" + userId;
     
        return this.webClient.get()
                .uri(endpoint)
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

    public UserDTO findByUsername(String username, String token) {
        String endpoint = "/user/username/" + username;

        return this.webClient.get()
                .uri(endpoint)
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

    public UsersDetailsDTO getUserByUsername(String username, String token) {
        return this.webClient.get()
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
                .bodyToMono(UsersDetailsDTO.class)
                .block();
    }
    
    public Long getUserIdByUsername(String username, String token) {
        try {
            UserDTO user = this.webClient.get()
                    .uri("/user/username/{username}", username)
                    .headers(headers -> headers.setBearerAuth(token))
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
                    .bodyToMono(UserDTO.class)
                    .block();

            return user != null ? user.getId() : null;

        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve userId for username: " + username, e);
        }
    }

}
