package com.pesco.wallet_service.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pesco.wallet_service.dtos.AdminDTO;
import com.pesco.wallet_service.exceptions.UserClientNotFoundException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class AdminServiceClient {

    private final WebClient webClient;

    public AdminServiceClient(@Qualifier("adminServiceWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public AdminDTO findAdminByUsername(String username, String token) {
        return this.webClient.get()
                .uri("/admin/api/username/{username}", username)
                .headers(headers -> headers.setBearerAuth(token))
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorMessage -> {
                                    if (clientResponse.statusCode().is4xxClientError()) {
                                        String details = extractDetailsFromError(errorMessage);
                                        return Mono.error(new UserClientNotFoundException("Admin not found", details));
                                    }
                                    return Mono.error(new RuntimeException("Server error"));
                                }))
                .bodyToMono(AdminDTO.class)
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