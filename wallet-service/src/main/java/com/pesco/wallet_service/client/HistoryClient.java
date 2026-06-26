package com.pesco.wallet_service.client;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.pesco.wallet_service.dtos.HistoryDTO;
import com.pesco.wallet_service.exceptions.UserClientNotFoundException;
import com.pesco.wallet_service.payloads.SwapHistoryRequest;
import com.pesco.wallet_service.response.HistoryResponse;
import reactor.core.publisher.Mono;

@Service
public class HistoryClient {

    private final WebClient historyServiceWebClient;

    public HistoryClient(@Qualifier("historyServiceWebClient") WebClient historyServiceWebClient) {
        this.historyServiceWebClient = historyServiceWebClient;
    }

    
    public CompletableFuture<HistoryResponse> findByUserId(Long userId, String token) {
        return historyServiceWebClient.get()
            .uri("/history/user/{userId}", userId)
            .header("Authorization", "Bearer " + token)
            .retrieve()
            .bodyToMono(HistoryDTO[].class)
            .map(historyArray -> {
                List<HistoryDTO> historyList = Arrays.asList(historyArray);
                return new HistoryResponse(true, historyList);
            })
            .onErrorResume(error -> {
                System.err.println("Error fetching history for user " + userId + ": " + error.getMessage());
                return Mono.just(new HistoryResponse(false, "Failed to fetch history: " + error.getMessage()));
            })
            .toFuture();
    }

    public void createUserHistory(SwapHistoryRequest request, String token) {
        historyServiceWebClient.post()
            .uri("/history/create/swap")
            .header("Authorization", "Bearer " + token)
            .bodyValue(request)
            .retrieve()
            .onStatus(
                status -> status.is4xxClientError(),
                clientResponse -> clientResponse.bodyToMono(String.class)
                    .flatMap(errorBody -> Mono.error(
                        new UserClientNotFoundException("History not created", errorBody)
                    ))
            )
            .onStatus(
                status -> status.is5xxServerError(),
                clientResponse -> clientResponse.bodyToMono(String.class)
                    .flatMap(errorBody -> Mono.error(
                        new RuntimeException("History service error: " + errorBody)
                    ))
            )
            .bodyToMono(Void.class)
            .onErrorResume(e -> Mono.empty())
            .subscribe();
    }

}