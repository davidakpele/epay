package pesco.deposit_service.clients;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import pesco.deposit_service.dto.BankListDTO;
import pesco.deposit_service.exceptions.BanKDetailsNotFound;
import reactor.core.publisher.Mono;

@Service
public class BankListServiceClient {

    private final WebClient bankListServiceWebClient;

    public BankListServiceClient(WebClient bankListServiceWebClient) {
        this.bankListServiceWebClient = bankListServiceWebClient;
    }

    public BankListDTO findByAccountNumber(String accountNumber, String token) {
        return this.bankListServiceWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/bank/accounts/{accountNumber}")
                        .build(accountNumber))
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorMessage -> {
                                    if (clientResponse.statusCode().is4xxClientError()) {
                                        String details = extractDetailsFromError(errorMessage);
                                        return Mono.error(new BanKDetailsNotFound(
                                                "Bank details not found", details));
                                    }
                                    return Mono.error(new RuntimeException("Server error while fetching data"));
                                }))
                .bodyToMono(BankListDTO.class)
                .block();
    }

    private String extractDetailsFromError(String errorMessage) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(errorMessage);
            return rootNode.path("message").asText("No message provided");
        } catch (JsonProcessingException e) {
            return errorMessage != null && !errorMessage.isEmpty()
                    ? errorMessage
                    : "No details available";
        }
    }
}
