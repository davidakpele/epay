package pesco.deposit_service.clients;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import pesco.deposit_service.exceptions.UserClientNotFoundException;
import pesco.deposit_service.payloads.DepositHistoryRequest;
import reactor.core.publisher.Mono;


@Service
public class HistoryServiceClient {

    private static final Logger log = LoggerFactory.getLogger(HistoryServiceClient.class);
    private final WebClient historyServiceWebClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public HistoryServiceClient(WebClient historyServiceWebClient) {
        this.historyServiceWebClient = historyServiceWebClient;
    }

    public void createDepositHistory(DepositHistoryRequest historyRequest, String token) {
    long startTime = System.currentTimeMillis();

    try {
        String payload = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(historyRequest);
        log.info("[HistoryServiceClient] - Outgoing payload:\n{}", payload);
    } catch (JsonProcessingException e) {
        log.warn("[HistoryServiceClient] - Could not serialize payload: {}", e.getMessage());
    }

    this.historyServiceWebClient.post()
        .uri("/history/create/deposit")
        .headers(headers -> {
            if (token != null && !token.isBlank()) {
                headers.setBearerAuth(token);
                log.debug("[HistoryServiceClient] Authorization header set.");
            } else {
                log.warn("[HistoryServiceClient] - No token provided.");
            }
        })
        .bodyValue(historyRequest)
        .exchangeToMono(clientResponse -> {
            int statusCode = clientResponse.statusCode().value();
            log.info("[HistoryServiceClient] - Response received | Status: {}", statusCode);

            if (clientResponse.statusCode().is2xxSuccessful()) {
                log.info("[HistoryServiceClient] - History saved successfully | Status: {}", statusCode);
                return clientResponse.releaseBody().then(Mono.empty());
            }

            return clientResponse.bodyToMono(String.class)
                    .flatMap(errorBody -> {
                        log.error("[HistoryServiceClient] - FAILED | Status: {} | Body: {}", statusCode, errorBody);

                        if (clientResponse.statusCode().is4xxClientError()) {
                            String details = extractDetailsFromError(errorBody);
                            log.warn("[HistoryServiceClient] 4xx details: {}", details);
                            return Mono.<Void>error(new UserClientNotFoundException("History not created", details));
                        }

                        return Mono.<Void>error(new RuntimeException("Server error: " + errorBody));
                    });
        })
        .doOnSuccess(v -> log.info("[HistoryServiceClient] - Pipeline completed successfully"))
        .doOnError(err -> log.error("[HistoryServiceClient] - Error: {} | Message: {}",
                err.getClass().getSimpleName(), err.getMessage()))
        .doFinally(signal -> {
            long elapsed = System.currentTimeMillis() - startTime;
            log.info("[HistoryServiceClient] - Finished | Signal: {} | Duration: {}ms", signal, elapsed);
        })
        .block();
}

    private String extractDetailsFromError(String errorMessage) {
        log.debug("[HistoryServiceClient] Parsing error body: {}", errorMessage);
        try {
            JsonNode rootNode = objectMapper.readTree(errorMessage);
            String details = rootNode.path("message").asText();
            log.debug("[HistoryServiceClient] Extracted error details: {}", details);
            return details;
        } catch (JsonProcessingException e) {
            log.warn("[HistoryServiceClient] Failed to parse error body as JSON: {}", e.getMessage());
            return "No details available";
        }
    }

}