package pesco.example.withdraw_service.clients;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
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
import reactor.core.publisher.Mono;


@Service
public class HistoryServiceClient {

    private final WebClient historyServiceWebClient;

    public HistoryServiceClient(WebClient historyServiceWebClient) {
        this.historyServiceWebClient = historyServiceWebClient;
    }
    
    public List<HistoryDTO> FindRecentTransactionsByUserId(Long id, LocalDateTime minusMinutes, String token) {
        try {
            LocalDateTime now = LocalDateTime.now();
            long minutes = Duration.between(minusMinutes, now).toMinutes();
       
            if (minutes <= 0) { 
                return Collections.emptyList();
            }

            String url = "/history/user/{id}/transactions/recent?minutes={minutes}";
    
            List<HistoryDTO> historyList = this.historyServiceWebClient.get()
                    .uri(url, id, minutes)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> {
                                return clientResponse.bodyToMono(String.class)
                                        .flatMap(errorMessage -> {
                                            if (clientResponse.statusCode().is4xxClientError()) {
                                                String details = extractDetailsFromError(errorMessage);
                                                return Mono.error(new UserClientNotFoundException("User not found", details));
                                            }
                                            return Mono.error(new RuntimeException("Server error"));
                                        });
                            })
                    .bodyToMono(new ParameterizedTypeReference<List<HistoryDTO>>() {})
                    .block();
            return historyList;
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    public List<HistoryDTO> FindByWalletIdAndUserId(Long userId, Long walletId, String token) {
        try {
            String url = "/history/wallet/{walletId}/userId/{userId}/transactions";
            List<HistoryDTO> historyList = this.historyServiceWebClient.get()
                    .uri(url, walletId, userId) 
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> {
                                return clientResponse.bodyToMono(String.class)
                                        .flatMap(errorMessage -> {
                                            if (clientResponse.statusCode().is4xxClientError()) {
                                                String details = extractDetailsFromError(errorMessage);
                                                return Mono.error(new UserClientNotFoundException("Wallet not found", details));
                                            }
                                            return Mono.error(new RuntimeException("Server error"));
                                        });
                            })
                    .bodyToMono(new ParameterizedTypeReference<List<HistoryDTO>>() {})
                    .block();
            return historyList;
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    public List<HistoryDTO> FindByTimestampAfterAndWalletId(Instant minus, Long walletId, String token) {
        try {
            String url = "/history/wallet/{walletId}/transactions/timestamp";
            List<HistoryDTO> historyList = this.historyServiceWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(url)
                            .queryParam("timestamp", minus.toString())
                            .build(walletId))
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> {
                                return clientResponse.bodyToMono(String.class)
                                        .flatMap(errorMessage -> {
                                            if (clientResponse.statusCode().is4xxClientError()) {
                                                String details = extractDetailsFromError(errorMessage);
                                                return Mono.error(new UserClientNotFoundException("Wallet not found", details));
                                            }
                                            return Mono.error(new RuntimeException("Server error"));
                                        });
                            })
                    .bodyToMono(new ParameterizedTypeReference<List<HistoryDTO>>() {})
                    .block();
            return historyList;
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    public HistoryDTO createUserCreditHistory(WithdrawHistoryRequestDTO recipientHistory, String token) {
        try {
            String url = "/history/create/withdrawal";
            HistoryDTO historyDto = this.historyServiceWebClient.post()
                    .uri(url)
                    .header("Authorization", "Bearer " + token)
                    .bodyValue(recipientHistory)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> {
                            return clientResponse.bodyToMono(String.class)
                                    .flatMap(errorMessage -> {
                                        if (clientResponse.statusCode().is4xxClientError()) {
                                            String details = extractDetailsFromError(errorMessage);
                                            return Mono.error(new UserClientNotFoundException("History not created", details));
                                        }
                                        return Mono.error(new RuntimeException("Server error"));
                                    });
                        })
                    .bodyToMono(HistoryDTO.class)
                    .block();
            return historyDto;
        } catch (Exception ex) {
            return null;
        }
    }

    public HistoryDTO createUserDebitHistory(WithdrawHistoryRequestDTO recipientHistory, String token) {
        try {
            String url = "/history/create/credit";
            HistoryDTO history = this.historyServiceWebClient.post()
                    .uri(url)
                    .header("Authorization", "Bearer " + token)
                    .bodyValue(recipientHistory)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> {
                            return clientResponse.bodyToMono(String.class)
                                    .flatMap(errorMessage -> {
                                        if (clientResponse.statusCode().is4xxClientError()) {
                                            String details = extractDetailsFromError(errorMessage);
                                            return Mono.error(new UserClientNotFoundException("History not created", details));
                                        }
                                        return Mono.error(new RuntimeException("Server error"));
                                    });
                        })
                    .bodyToMono(HistoryDTO.class)
                    .block();
            return history;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public List<HistoryDTO> listHistoryByType(Long id, String username, TransactionType deposit, String token) {
        try {
            String url = "/history/wallet/{walletId}/userId/{id}/transactions";
            List<HistoryDTO> historyList = this.historyServiceWebClient.get()
                    .uri(url, id, id)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> {
                                return clientResponse.bodyToMono(String.class)
                                        .flatMap(errorMessage -> {
                                            if (clientResponse.statusCode().is4xxClientError()) {
                                                String details = extractDetailsFromError(errorMessage);
                                                return Mono.error(new UserClientNotFoundException("Wallet not found", details));
                                            }
                                            return Mono.error(new RuntimeException("Server error"));
                                        });
                            })
                    .bodyToMono(new ParameterizedTypeReference<List<HistoryDTO>>() {})
                    .block();
            return historyList;
        } catch (Exception ex) {
            ex.printStackTrace();
            return Collections.emptyList();
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