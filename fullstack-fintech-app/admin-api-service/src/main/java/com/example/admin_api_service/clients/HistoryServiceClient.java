package com.example.admin_api_service.clients;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class HistoryServiceClient {

    private final WebClient webClient;

    public HistoryServiceClient(@Qualifier("historyServiceWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public Long getTotalHistory(String token) {  
        return this.webClient.get()
                .uri("/history/count")
                .headers(headers -> headers.setBearerAuth(token))
                .exchangeToMono(response -> {
                    System.out.println(">>> Sending request to: " + response.request().getURI());
                    return response.bodyToMono(Long.class);
                })
                .block();
    }
}