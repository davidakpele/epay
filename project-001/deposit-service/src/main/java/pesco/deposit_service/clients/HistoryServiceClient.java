package pesco.deposit_service.clients;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import pesco.deposit_service.exceptions.UserClientNotFoundException;
import pesco.deposit_service.payloads.DepositHistoryRequest;
import pesco.deposit_service.payloads.HistoryRequest;
import reactor.core.publisher.Mono;

@Service
public class HistoryServiceClient {

    private final WebClient historyServiceWebClient;

    public HistoryServiceClient(WebClient historyServiceWebClient) {
        this.historyServiceWebClient = historyServiceWebClient;
    }
 
    public HistoryRequest createDepositHistory(DepositHistoryRequest historyRequest, String token) {
       try {
            return this.historyServiceWebClient.post()
                .uri("/history/create/deposit")
                .bodyValue(historyRequest) 
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorMessage -> {
                                    if (clientResponse.statusCode().is4xxClientError()) {
                                        String details = extractDetailsFromError(errorMessage);
                                        return Mono
                                                .error(new UserClientNotFoundException("History not created",
                                                        details));
                                    }
                                    return Mono.error(new RuntimeException("Server error"));
                                }))
                .bodyToMono(HistoryRequest.class) 
                .block();
        } catch (Exception ex) {
            System.err.println("Error creating user credit history: " + ex.getMessage());
            return null; 
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

}
