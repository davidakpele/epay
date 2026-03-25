package pesco.example.withdraw_service.clients;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import pesco.example.withdraw_service.dtos.HistoryDTO;
import pesco.example.withdraw_service.dtos.WithdrawHistoryRequestDTO;
import pesco.example.withdraw_service.enums.TransactionType;
import pesco.example.withdraw_service.exceptions.UserClientNotFoundException;
import pesco.example.withdraw_service.payloads.CreditHistoryRequest;
import reactor.core.publisher.Mono;


@Service
public class HistoryServiceClient {

    private static final Logger log = LoggerFactory.getLogger(HistoryServiceClient.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final WebClient historyServiceWebClient;

    public HistoryServiceClient(WebClient historyServiceWebClient) {
        this.historyServiceWebClient = historyServiceWebClient;
    }

    public List<HistoryDTO> FindRecentTransactionsByUserId(Long id, LocalDateTime minusMinutes, String token) {
        String method = "FindRecentTransactionsByUserId";
        long startTime = System.currentTimeMillis();
        log.info("[HistoryServiceClient] [{}] START | userId={} | since={}", method, id, minusMinutes);

        try {
            LocalDateTime now = LocalDateTime.now();
            long minutes = Duration.between(minusMinutes, now).toMinutes();

            if (minutes <= 0) {
                log.warn("[HistoryServiceClient] [{}] SKIPPED | userId={} | reason=non-positive duration | minutes={}", method, id, minutes);
                return Collections.emptyList();
            }

            String url = "/history/user/{id}/transactions/recent?minutes={minutes}";
            log.debug("[HistoryServiceClient] [{}] REQUEST | GET {} | userId={} | minutes={}", method, url, id, minutes);

            List<HistoryDTO> historyList = this.historyServiceWebClient.get()
                    .uri(url, id, minutes)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(errorMessage -> {
                                        int statusCode = clientResponse.statusCode().value();
                                        if (clientResponse.statusCode().is4xxClientError()) {
                                            String details = extractDetailsFromError(errorMessage);
                                            log.error("[HistoryServiceClient] [{}] FAILED | 4xx | userId={} | status={} | details={}", method, id, statusCode, details);
                                            return Mono.error(new UserClientNotFoundException("User not found", details));
                                        }
                                        log.error("[HistoryServiceClient] [{}] FAILED | 5xx | userId={} | status={} | body={}", method, id, statusCode, errorMessage);
                                        return Mono.error(new RuntimeException("Server error"));
                                    }))
                    .bodyToMono(new ParameterizedTypeReference<List<HistoryDTO>>() {})
                    .block();

            long elapsed = System.currentTimeMillis() - startTime;
            int resultCount = historyList != null ? historyList.size() : 0;
            log.info("[HistoryServiceClient] [{}] SUCCESS | userId={} | records={} | duration={}ms", method, id, resultCount, elapsed);
            return historyList;

        } catch (Exception ex) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.error("[HistoryServiceClient] [{}] ERROR | userId={} | duration={}ms | exception={} | message={}",
                    method, id, elapsed, ex.getClass().getSimpleName(), ex.getMessage());
            return Collections.emptyList();
        }
    }

    public List<HistoryDTO> FindByWalletIdAndUserId(Long userId, Long walletId, String token) {
        String method = "FindByWalletIdAndUserId";
        long startTime = System.currentTimeMillis();
        log.info("[HistoryServiceClient] [{}] START | userId={} | walletId={}", method, userId, walletId);

        try {
            String url = "/history/wallet/{walletId}/userId/{userId}/transactions";
            log.debug("[HistoryServiceClient] [{}] REQUEST | GET {} | walletId={} | userId={}", method, url, walletId, userId);

            List<HistoryDTO> historyList = this.historyServiceWebClient.get()
                    .uri(url, walletId, userId)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(errorMessage -> {
                                        int statusCode = clientResponse.statusCode().value();
                                        if (clientResponse.statusCode().is4xxClientError()) {
                                            String details = extractDetailsFromError(errorMessage);
                                            log.error("[HistoryServiceClient] [{}] FAILED | 4xx | userId={} | walletId={} | status={} | details={}", method, userId, walletId, statusCode, details);
                                            return Mono.error(new UserClientNotFoundException("Wallet not found", details));
                                        }
                                        log.error("[HistoryServiceClient] [{}] FAILED | 5xx | userId={} | walletId={} | status={} | body={}", method, userId, walletId, statusCode, errorMessage);
                                        return Mono.error(new RuntimeException("Server error"));
                                    }))
                    .bodyToMono(new ParameterizedTypeReference<List<HistoryDTO>>() {})
                    .block();

            long elapsed = System.currentTimeMillis() - startTime;
            int resultCount = historyList != null ? historyList.size() : 0;
            log.info("[HistoryServiceClient] [{}] SUCCESS | userId={} | walletId={} | records={} | duration={}ms", method, userId, walletId, resultCount, elapsed);
            return historyList;

        } catch (Exception ex) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.error("[HistoryServiceClient] [{}] ERROR | userId={} | walletId={} | duration={}ms | exception={} | message={}",
                    method, userId, walletId, elapsed, ex.getClass().getSimpleName(), ex.getMessage());
            return Collections.emptyList();
        }
    }

    public List<HistoryDTO> FindByTimestampAfterAndWalletId(Instant minus, Long walletId, String token) {
        String method = "FindByTimestampAfterAndWalletId";
        long startTime = System.currentTimeMillis();
        log.info("[HistoryServiceClient] [{}] START | walletId={} | since={}", method, walletId, minus);

        try {
            String url = "/history/wallet/{walletId}/transactions/timestamp";
            log.debug("[HistoryServiceClient] [{}] REQUEST | GET {} | walletId={} | timestamp={}", method, url, walletId, minus);

            List<HistoryDTO> historyList = this.historyServiceWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(url)
                            .queryParam("timestamp", minus.toString())
                            .build(walletId))
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(errorMessage -> {
                                        int statusCode = clientResponse.statusCode().value();
                                        if (clientResponse.statusCode().is4xxClientError()) {
                                            String details = extractDetailsFromError(errorMessage);
                                            log.error("[HistoryServiceClient] [{}] FAILED | 4xx | walletId={} | status={} | details={}", method, walletId, statusCode, details);
                                            return Mono.error(new UserClientNotFoundException("Wallet not found", details));
                                        }
                                        log.error("[HistoryServiceClient] [{}] FAILED | 5xx | walletId={} | status={} | body={}", method, walletId, statusCode, errorMessage);
                                        return Mono.error(new RuntimeException("Server error"));
                                    }))
                    .bodyToMono(new ParameterizedTypeReference<List<HistoryDTO>>() {})
                    .block();

            long elapsed = System.currentTimeMillis() - startTime;
            int resultCount = historyList != null ? historyList.size() : 0;
            log.info("[HistoryServiceClient] [{}] SUCCESS | walletId={} | records={} | duration={}ms", method, walletId, resultCount, elapsed);
            return historyList;

        } catch (Exception ex) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.error("[HistoryServiceClient] [{}] ERROR | walletId={} | duration={}ms | exception={} | message={}",
                    method, walletId, elapsed, ex.getClass().getSimpleName(), ex.getMessage());
            return Collections.emptyList();
        }
    }

    public HistoryDTO createUserCreditHistory(CreditHistoryRequest request, String token) {
        String method = "createUserCreditHistory";
        long startTime = System.currentTimeMillis();
        log.info("[HistoryServiceClient] [{}] START", method);

        try {
            String payload = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(request);
            log.info("[HistoryServiceClient] [{}] PAYLOAD:\n{}", method, payload);
        } catch (JsonProcessingException e) {
            log.warn("[HistoryServiceClient] [{}] Could not serialize payload: {}", method, e.getMessage());
        }

        if (token == null || token.isBlank()) {
            log.warn("[HistoryServiceClient] [{}] WARNING | No token provided", method);
        }

        try {
            HistoryDTO historyDto = this.historyServiceWebClient.post()
                    .uri("/history/create/withdrawal")
                    .headers(headers -> {
                        if (token != null && !token.isBlank()) {
                            headers.setBearerAuth(token);
                            log.debug("[HistoryServiceClient] [{}] Authorization header set", method);
                        }
                    })
                    .bodyValue(request)
                    .exchangeToMono(clientResponse -> {
                        int statusCode = clientResponse.statusCode().value();
                        log.info("[HistoryServiceClient] [{}] RESPONSE | status={}", method, statusCode);

                        if (clientResponse.statusCode().is2xxSuccessful()) {
                            log.info("[HistoryServiceClient] [{}] HTTP SUCCESS | status={}", method, statusCode);
                            return clientResponse.bodyToMono(HistoryDTO.class);
                        }

                        return clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error("[HistoryServiceClient] [{}] FAILED | status={} | body={}", method, statusCode, errorBody);
                                    if (clientResponse.statusCode().is4xxClientError()) {
                                        String details = extractDetailsFromError(errorBody);
                                        log.warn("[HistoryServiceClient] [{}] 4xx details: {}", method, details);
                                        return Mono.<HistoryDTO>error(new UserClientNotFoundException("History not created", details));
                                    }
                                    return Mono.<HistoryDTO>error(new RuntimeException("Server error: " + errorBody));
                                });
                    })
                    .doOnSuccess(v -> {
                        long elapsed = System.currentTimeMillis() - startTime;
                        log.info("[HistoryServiceClient] [{}] SUCCESS | duration={}ms | historyId={}", method, elapsed, v != null ? v.getId() : "null");
                    })
                    .doOnError(err -> log.error("[HistoryServiceClient] [{}] PIPELINE ERROR | exception={} | message={}",
                            method, err.getClass().getSimpleName(), err.getMessage()))
                    .doFinally(signal -> {
                        long elapsed = System.currentTimeMillis() - startTime;
                        log.info("[HistoryServiceClient] [{}] FINISHED | signal={} | duration={}ms", method, signal, elapsed);
                    })
                    .block();

            return historyDto;

        } catch (Exception ex) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.error("[HistoryServiceClient] [{}] ERROR | duration={}ms | exception={} | message={}",
                    method, elapsed, ex.getClass().getSimpleName(), ex.getMessage());
            return null;
        }
    }

    public HistoryDTO createUserDebitHistory(WithdrawHistoryRequestDTO recipientHistory, String token) {
        String method = "createUserDebitHistory";
        long startTime = System.currentTimeMillis();
        log.info("[HistoryServiceClient] [{}] START", method);

        try {
            String payload = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(recipientHistory);
            log.debug("[HistoryServiceClient] [{}] PAYLOAD:\n{}", method, payload);
        } catch (JsonProcessingException e) {
            log.warn("[HistoryServiceClient] [{}] Could not serialize payload: {}", method, e.getMessage());
        }

        try {
            log.debug("[HistoryServiceClient] [{}] REQUEST | POST /history/create/credit", method);

            HistoryDTO history = this.historyServiceWebClient.post()
                    .uri("/history/create/credit")
                    .header("Authorization", "Bearer " + token)
                    .bodyValue(recipientHistory)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(errorMessage -> {
                                        int statusCode = clientResponse.statusCode().value();
                                        if (clientResponse.statusCode().is4xxClientError()) {
                                            String details = extractDetailsFromError(errorMessage);
                                            log.error("[HistoryServiceClient] [{}] FAILED | 4xx | status={} | details={}", method, statusCode, details);
                                            return Mono.error(new UserClientNotFoundException("History not created", details));
                                        }
                                        log.error("[HistoryServiceClient] [{}] FAILED | 5xx | status={} | body={}", method, statusCode, errorMessage);
                                        return Mono.error(new RuntimeException("Server error"));
                                    }))
                    .bodyToMono(HistoryDTO.class)
                    .block();

            long elapsed = System.currentTimeMillis() - startTime;
            log.info("[HistoryServiceClient] [{}] SUCCESS | duration={}ms | historyId={}", method, elapsed, history != null ? history.getId() : "null");
            return history;

        } catch (Exception ex) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.error("[HistoryServiceClient] [{}] ERROR | duration={}ms | exception={} | message={}",
                    method, elapsed, ex.getClass().getSimpleName(), ex.getMessage());
            return null;
        }
    }

    public List<HistoryDTO> listHistoryByType(Long id, String username, TransactionType deposit, String token) {
        String method = "listHistoryByType";
        long startTime = System.currentTimeMillis();
        log.info("[HistoryServiceClient] [{}] START | userId={} | username={} | type={}", method, id, username, deposit);

        try {
            String url = "/history/wallet/{walletId}/userId/{id}/transactions";
            log.debug("[HistoryServiceClient] [{}] REQUEST | GET {} | walletId={} | userId={}", method, url, id, id);

            List<HistoryDTO> historyList = this.historyServiceWebClient.get()
                    .uri(url, id, id)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(errorMessage -> {
                                        int statusCode = clientResponse.statusCode().value();
                                        if (clientResponse.statusCode().is4xxClientError()) {
                                            String details = extractDetailsFromError(errorMessage);
                                            log.error("[HistoryServiceClient] [{}] FAILED | 4xx | userId={} | type={} | status={} | details={}", method, id, deposit, statusCode, details);
                                            return Mono.error(new UserClientNotFoundException("Wallet not found", details));
                                        }
                                        log.error("[HistoryServiceClient] [{}] FAILED | 5xx | userId={} | type={} | status={} | body={}", method, id, deposit, statusCode, errorMessage);
                                        return Mono.error(new RuntimeException("Server error"));
                                    }))
                    .bodyToMono(new ParameterizedTypeReference<List<HistoryDTO>>() {})
                    .block();

            long elapsed = System.currentTimeMillis() - startTime;
            int resultCount = historyList != null ? historyList.size() : 0;
            log.info("[HistoryServiceClient] [{}] SUCCESS | userId={} | type={} | records={} | duration={}ms", method, id, deposit, resultCount, elapsed);
            return historyList;

        } catch (Exception ex) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.error("[HistoryServiceClient] [{}] ERROR | userId={} | type={} | duration={}ms | exception={} | message={}",
                    method, id, deposit, elapsed, ex.getClass().getSimpleName(), ex.getMessage());
            return Collections.emptyList();
        }
    }

    private String extractDetailsFromError(String errorMessage) {
        try {
            JsonNode rootNode = objectMapper.readTree(errorMessage);
            return rootNode.path("message").asText();
        } catch (JsonProcessingException e) {
            log.warn("[HistoryServiceClient] [extractDetailsFromError] Failed to parse error body as JSON: {}", e.getMessage());
            return "No details available";
        }
    }
}