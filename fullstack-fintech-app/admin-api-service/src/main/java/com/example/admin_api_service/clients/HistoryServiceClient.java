package com.example.admin_api_service.clients;

import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.example.admin_api_service.responses.AnalyticsDataPoint;
import com.example.admin_api_service.responses.TransactionHistory;

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

    public List<AnalyticsDataPoint> getTransactionAnalytics(String token, String period) {
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/history/analytics")
                        .queryParam("period", period)
                        .build())
                .headers(h -> h.setBearerAuth(token))
                .exchangeToMono(response -> {
                    System.out.println(">>> Sending request to: " + response.request().getURI());
                    return response.bodyToFlux(AnalyticsDataPoint.class)
                            .collectList()
                            .map(list -> (List<AnalyticsDataPoint>) list);
                })
                .block();
    }

    public List<TransactionHistory> getRecentHistory(String token, int limit) {
        return this.webClient.get()
            .uri(uriBuilder -> uriBuilder
                    .path("/history/recent")
                    .queryParam("limit", limit)
                    .build())
            .headers(h -> h.setBearerAuth(token))
            .exchangeToMono(response -> {
                System.out.println(">>> Sending request to: " + response.request().getURI());
                return response.bodyToFlux(TransactionHistory.class)
                        .collectList()
                        .map(list -> (List<TransactionHistory>) list);
            }).block();
    }

}