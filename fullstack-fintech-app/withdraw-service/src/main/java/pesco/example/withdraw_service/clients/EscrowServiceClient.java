package pesco.example.withdraw_service.clients;

import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import pesco.example.withdraw_service.payloads.CreateEscrowRequest;

@Component
public class EscrowServiceClient {

    private final WebClient escrowServiceWebClient;

    public EscrowServiceClient(@Qualifier("escrowServiceWebClient") WebClient escrowServiceWebClient) {
        this.escrowServiceWebClient = escrowServiceWebClient;
    }

    public ResponseEntity<?> create(CreateEscrowRequest escrowRequest) {
        System.out.println("Process Escrow request");
        try {
            System.out.println("Sending request to escrow service");
            return this.escrowServiceWebClient.post()
                    .uri("/api/escrow")
                    .bodyValue(escrowRequest)
                    .retrieve()
                    .toEntity(Object.class)
                    .block();
        } catch (Exception e) {
            System.out.println("Error calling escrow service: " + e.getMessage());
            return ResponseEntity.status(500).body("Error creating escrow");
        }
    }

    public ResponseEntity<?> updateLedgerStatus(String ledgerId, String status) {
        try {
            Map<String, String> statusUpdate = Map.of("status", status);
            System.out.println("Process Update status Escrow request");
            return this.escrowServiceWebClient.put()
                    .uri("/api/escrow/{ledgerId}/status", ledgerId)
                    .bodyValue(statusUpdate)
                    .retrieve()
                    .toEntity(Object.class)
                    .block();
        } catch (Exception e) {
            System.out.println("Error updating ledger status: " + e.getMessage());
            return ResponseEntity.status(500).body("Error updating ledger status");
        }
    }
}