package com.example.escrow_service.clients;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import com.example.escrow_service.payloads.WalletRefundRequest;
import reactor.core.publisher.Mono;

@Component
public class WalletServiceClient {
    
    private final WebClient walletServiceWebClient;

    public WalletServiceClient(@Qualifier("walletServiceWebClient") WebClient walletServiceWebClient) {
        this.walletServiceWebClient = walletServiceWebClient;
    }

    public boolean refundWallet(WalletRefundRequest refundRequest) {
        try {
            this.walletServiceWebClient.put()
                    .uri("/api/v1/wallet/refund")
                    .bodyValue(refundRequest) 
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(errorMessage -> {
                                        return Mono.error(new RuntimeException("Server error while crediting wallet"));
                                    }))
                    .bodyToMono(Void.class)
                    .block();
            
            return true;
            
        } catch (Exception e) {
            System.out.println("Error calling wallet service: " + e.getMessage());
            return false;
        }
    }
}