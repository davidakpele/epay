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
  
    public CompletableFuture<HistoryResponse> findByUserId(Long userId) {
        return historyServiceWebClient.get()
            .uri("/history/user/{userId}", userId)
            .retrieve()
            .bodyToMono(HistoryDTO[].class)
            .map(historyArray -> {
                List<HistoryDTO> historyList = Arrays.asList(historyArray);
                return new HistoryResponse(true, historyList);
            })
            .onErrorResume(error -> {
                System.err.println("❌ Error fetching history for user " + userId + ": " + error.getMessage());
                error.printStackTrace();
                return Mono.just(new HistoryResponse(false, "Failed to fetch history: " + error.getMessage()));
            })
            // .doOnSuccess(response -> {
            //     if (response.isSuccess()) {
            //         System.out.println("🎉 History fetch completed successfully for user " + userId);
            //     } else {
            //         System.out.println("⚠️ History fetch completed with errors for user " + userId);
            //     }
            // })
            .toFuture();
    }

    public void createUserHistory(SwapHistoryRequest request) {
        historyServiceWebClient.post()
            .uri("/history/create/swap")
            .bodyValue(request)
            .retrieve()
            .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                    clientResponse -> clientResponse.bodyToMono(String.class)
                            .flatMap(errorMessage -> {
                                if (clientResponse.statusCode().is4xxClientError()) {
                                    return Mono.error(new UserClientNotFoundException("History not created", errorMessage));
                                }
                                return Mono.error(new RuntimeException("Server error: " + errorMessage));
                            }))
            .bodyToMono(Void.class)
            // .doOnSuccess(response -> {
            //     System.out.println("✅ Swap history created successfully");
            // })
            .doOnError(error -> {
                System.err.println("❌ Error creating swap history: " + error.getMessage());
            })
            .subscribe();
    }
}