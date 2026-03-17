package com.example.admin_api_service.clients;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class VirtualCardServiceClient {

    private final WebClient webClient;

    public VirtualCardServiceClient(@Qualifier("virtualCardServiceWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public Long getTotalVirtualCards(String token) {
        return this.webClient.get()
                .uri("/virtual-cards/count")
                .headers(headers -> headers.setBearerAuth(token))
                .exchangeToMono(response -> {
                    System.out.println(">>> Sending request to: " + response.request().getURI());
                    return response.bodyToMono(Long.class);
                })
                .block();
    }
}
