package com.example.admin_api_service.clients;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.admin_api_service.dto.UserDTO;
import com.example.admin_api_service.exceptions.UserClientNotFoundException;
import com.example.admin_api_service.responses.UserStatisticsResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
                    UserDTO userDTO = convertToUserDTO(response);
                    return Mono.just(userDTO);
                })
                .switchIfEmpty(Mono.error(new UserClientNotFoundException("User not found: No data returned", token)))
                .block();

    }

    public Long getTotalUsers(String token) {
        String uri = "/user/count";

        return this.webClient.get()
                .uri(uri)
                .headers(headers -> headers.setBearerAuth(token))
                .exchangeToMono(response -> {
                    System.out.println(">>> Sending request to: " + response.request().getURI());
                    return response.bodyToMono(Long.class);
                })
                .block();
    }

    public UserDTO convertToUserDTO(String response) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            return objectMapper.readValue(response, UserDTO.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting response to UserDTO", e);
        }
    }

    public UserStatisticsResponse getUserStatistics(String token, String period) {
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/users/statistics")
                        .queryParam("period", period)
                        .build())
                .headers(h -> h.setBearerAuth(token))
                .retrieve()
                .bodyToMono(UserStatisticsResponse.class)
                .block();
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
}
