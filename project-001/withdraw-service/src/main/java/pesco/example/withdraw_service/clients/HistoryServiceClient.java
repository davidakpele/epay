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
        System.out.println("[HistoryClient] FindRecentTransactionsByUserId - START | userId=" + id + ", from=" + minusMinutes);
        try {
            LocalDateTime now = LocalDateTime.now();
            long minutes = Duration.between(minusMinutes, now).toMinutes();
            System.out.println("[HistoryClient] FindRecentTransactionsByUserId - Computed minutes=" + minutes);

            if (minutes <= 0) {
                System.out.println("[HistoryClient] FindRecentTransactionsByUserId - minutes <= 0, returning empty list");
                return Collections.emptyList();
            }

            String url = "/history/user/{id}/transactions/recent?minutes={minutes}";
            System.out.println("[HistoryClient] FindRecentTransactionsByUserId - Calling URL: " + url + " | id=" + id + ", minutes=" + minutes);
            long startTime = System.currentTimeMillis();

            List<HistoryDTO> historyList = this.historyServiceWebClient.get()
                    .uri(url, id, minutes)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> {
                                System.err.println("[HistoryClient] FindRecentTransactionsByUserId - HTTP ERROR: " + clientResponse.statusCode());
                                return clientResponse.bodyToMono(String.class)
                                        .flatMap(errorMessage -> {
                                            System.err.println("[HistoryClient] FindRecentTransactionsByUserId - Error body: " + errorMessage);
                                            if (clientResponse.statusCode().is4xxClientError()) {
                                                String details = extractDetailsFromError(errorMessage);
                                                return Mono.error(new UserClientNotFoundException("User not found", details));
                                            }
                                            return Mono.error(new RuntimeException("Server error"));
                                        });
                            })
                    .bodyToMono(new ParameterizedTypeReference<List<HistoryDTO>>() {})
                    .block();

            long elapsed = System.currentTimeMillis() - startTime;
            System.out.println("[HistoryClient] FindRecentTransactionsByUserId - SUCCESS | elapsed=" + elapsed + "ms | resultCount=" + (historyList != null ? historyList.size() : 0));
            return historyList;

        } catch (Exception ex) {
            System.err.println("[HistoryClient] FindRecentTransactionsByUserId - EXCEPTION: " + ex.getClass().getSimpleName() + " | message=" + ex.getMessage());
            ex.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<HistoryDTO> FindByWalletIdAndUserId(Long userId, Long walletId, String token) {
        System.out.println("[HistoryClient] FindByWalletIdAndUserId - START | userId=" + userId + ", walletId=" + walletId);
        try {
            String url = "/history/wallet/{walletId}/userId/{userId}/transactions";
            System.out.println("[HistoryClient] FindByWalletIdAndUserId - Calling URL: " + url + " | walletId=" + walletId + ", userId=" + userId);
            long startTime = System.currentTimeMillis();

            List<HistoryDTO> historyList = this.historyServiceWebClient.get()
                    .uri(url, walletId, userId) // NOTE: fixed arg order to match {walletId}/{userId}
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> {
                                System.err.println("[HistoryClient] FindByWalletIdAndUserId - HTTP ERROR: " + clientResponse.statusCode());
                                return clientResponse.bodyToMono(String.class)
                                        .flatMap(errorMessage -> {
                                            System.err.println("[HistoryClient] FindByWalletIdAndUserId - Error body: " + errorMessage);
                                            if (clientResponse.statusCode().is4xxClientError()) {
                                                String details = extractDetailsFromError(errorMessage);
                                                return Mono.error(new UserClientNotFoundException("Wallet not found", details));
                                            }
                                            return Mono.error(new RuntimeException("Server error"));
                                        });
                            })
                    .bodyToMono(new ParameterizedTypeReference<List<HistoryDTO>>() {})
                    .block();

            long elapsed = System.currentTimeMillis() - startTime;
            System.out.println("[HistoryClient] FindByWalletIdAndUserId - SUCCESS | elapsed=" + elapsed + "ms | resultCount=" + (historyList != null ? historyList.size() : 0));
            return historyList;

        } catch (Exception ex) {
            System.err.println("[HistoryClient] FindByWalletIdAndUserId - EXCEPTION: " + ex.getClass().getSimpleName() + " | message=" + ex.getMessage());
            ex.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<HistoryDTO> FindByTimestampAfterAndWalletId(Instant minus, Long walletId, String token) {
        System.out.println("[HistoryClient] FindByTimestampAfterAndWalletId - START | walletId=" + walletId + ", timestamp=" + minus);
        try {
            String url = "/history/wallet/{walletId}/transactions/timestamp";
            System.out.println("[HistoryClient] FindByTimestampAfterAndWalletId - Calling URL: " + url + " | walletId=" + walletId + ", timestamp=" + minus.toString());
            long startTime = System.currentTimeMillis();

            List<HistoryDTO> historyList = this.historyServiceWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(url)
                            .queryParam("timestamp", minus.toString())
                            .build(walletId))
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> {
                                System.err.println("[HistoryClient] FindByTimestampAfterAndWalletId - HTTP ERROR: " + clientResponse.statusCode());
                                return clientResponse.bodyToMono(String.class)
                                        .flatMap(errorMessage -> {
                                            System.err.println("[HistoryClient] FindByTimestampAfterAndWalletId - Error body: " + errorMessage);
                                            if (clientResponse.statusCode().is4xxClientError()) {
                                                String details = extractDetailsFromError(errorMessage);
                                                return Mono.error(new UserClientNotFoundException("Wallet not found", details));
                                            }
                                            return Mono.error(new RuntimeException("Server error"));
                                        });
                            })
                    .bodyToMono(new ParameterizedTypeReference<List<HistoryDTO>>() {})
                    .block();

            long elapsed = System.currentTimeMillis() - startTime;
            System.out.println("[HistoryClient] FindByTimestampAfterAndWalletId - SUCCESS | elapsed=" + elapsed + "ms | resultCount=" + (historyList != null ? historyList.size() : 0));
            return historyList;

        } catch (Exception ex) {
            System.err.println("[HistoryClient] FindByTimestampAfterAndWalletId - EXCEPTION: " + ex.getClass().getSimpleName() + " | message=" + ex.getMessage());
            ex.printStackTrace();
            return Collections.emptyList();
        }
    }

    public HistoryDTO createUserCreditHistory(WithdrawHistoryRequestDTO recipientHistory, String token) {
        System.out.println("[HistoryClient] createUserCreditHistory - START | payload=" + recipientHistory);
        try {
            String url = "/history/create/withdrawal";
            System.out.println("[HistoryClient] createUserCreditHistory - Calling URL: " + url);
            long startTime = System.currentTimeMillis();

            HistoryDTO historyDto = this.historyServiceWebClient.post()
                    .uri(url)
                    .header("Authorization", "Bearer " + token)
                    .bodyValue(recipientHistory)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> {
                                System.err.println("[HistoryClient] createUserCreditHistory - HTTP ERROR: " + clientResponse.statusCode());
                                return clientResponse.bodyToMono(String.class)
                                        .flatMap(errorMessage -> {
                                            System.err.println("[HistoryClient] createUserCreditHistory - Error body: " + errorMessage);
                                            if (clientResponse.statusCode().is4xxClientError()) {
                                                String details = extractDetailsFromError(errorMessage);
                                                return Mono.error(new UserClientNotFoundException("History not created", details));
                                            }
                                            return Mono.error(new RuntimeException("Server error"));
                                        });
                            })
                    .bodyToMono(HistoryDTO.class)
                    .block();

            long elapsed = System.currentTimeMillis() - startTime;
            System.out.println("[HistoryClient] createUserCreditHistory - SUCCESS | elapsed=" + elapsed + "ms | historyId=" + (historyDto != null ? historyDto.getId() : "null"));
            return historyDto;

        } catch (Exception ex) {
            System.err.println("[HistoryClient] createUserCreditHistory - EXCEPTION: " + ex.getClass().getSimpleName() + " | message=" + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    public HistoryDTO createUserDebitHistory(WithdrawHistoryRequestDTO recipientHistory, String token) {
        System.out.println("[HistoryClient] createUserDebitHistory - START | payload=" + recipientHistory);
        try {
            String url = "/history/create/credit";
            System.out.println("[HistoryClient] createUserDebitHistory - Calling URL: " + url);
            long startTime = System.currentTimeMillis();

            HistoryDTO history = this.historyServiceWebClient.post()
                    .uri(url)
                    .header("Authorization", "Bearer " + token)
                    .bodyValue(recipientHistory)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> {
                                System.err.println("[HistoryClient] createUserDebitHistory - HTTP ERROR: " + clientResponse.statusCode());
                                return clientResponse.bodyToMono(String.class)
                                        .flatMap(errorMessage -> {
                                            System.err.println("[HistoryClient] createUserDebitHistory - Error body: " + errorMessage);
                                            if (clientResponse.statusCode().is4xxClientError()) {
                                                String details = extractDetailsFromError(errorMessage);
                                                return Mono.error(new UserClientNotFoundException("History not created", details));
                                            }
                                            return Mono.error(new RuntimeException("Server error"));
                                        });
                            })
                    .bodyToMono(HistoryDTO.class)
                    .block();

            long elapsed = System.currentTimeMillis() - startTime;
            System.out.println("[HistoryClient] createUserDebitHistory - SUCCESS | elapsed=" + elapsed + "ms | historyId=" + (history != null ? history.getId() : "null"));
            return history;

        } catch (Exception ex) {
            System.err.println("[HistoryClient] createUserDebitHistory - EXCEPTION: " + ex.getClass().getSimpleName() + " | message=" + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    public List<HistoryDTO> listHistoryByType(Long id, String username, TransactionType deposit) {
        System.out.println("[HistoryClient] listHistoryByType - START | id=" + id + ", username=" + username + ", type=" + deposit);
        try {
            String url = "/history/wallet/{walletId}/userId/{id}/transactions";
            System.out.println("[HistoryClient] listHistoryByType - Calling URL: " + url + " | id=" + id);
            long startTime = System.currentTimeMillis();

            List<HistoryDTO> historyList = this.historyServiceWebClient.get()
                    .uri(url, id, id)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> {
                                System.err.println("[HistoryClient] listHistoryByType - HTTP ERROR: " + clientResponse.statusCode());
                                return clientResponse.bodyToMono(String.class)
                                        .flatMap(errorMessage -> {
                                            System.err.println("[HistoryClient] listHistoryByType - Error body: " + errorMessage);
                                            if (clientResponse.statusCode().is4xxClientError()) {
                                                String details = extractDetailsFromError(errorMessage);
                                                return Mono.error(new UserClientNotFoundException("Wallet not found", details));
                                            }
                                            return Mono.error(new RuntimeException("Server error"));
                                        });
                            })
                    .bodyToMono(new ParameterizedTypeReference<List<HistoryDTO>>() {})
                    .block();

            long elapsed = System.currentTimeMillis() - startTime;
            System.out.println("[HistoryClient] listHistoryByType - SUCCESS | elapsed=" + elapsed + "ms | resultCount=" + (historyList != null ? historyList.size() : 0));
            return historyList;

        } catch (Exception ex) {
            System.err.println("[HistoryClient] listHistoryByType - EXCEPTION: " + ex.getClass().getSimpleName() + " | message=" + ex.getMessage());
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