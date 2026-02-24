package com.example.escrow_service.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.escrow_service.payloads.CreateEscrowRequest;
import com.example.escrow_service.services.EscrowService;

@RestController
@RequestMapping("/api/escrow")
public class EscrowController {

    private final EscrowService escrowService;

    public EscrowController(EscrowService escrowService) {
        this.escrowService = escrowService;
    }

    @GetMapping("/ledgers")
    public ResponseEntity<?> getAllLedgers() {
        return escrowService.fetchAllLedgers();
    }

    @PostMapping
    public ResponseEntity<?> createEscrow(@RequestBody CreateEscrowRequest request) {
        return escrowService.create(request);
    }

    @GetMapping("/{ledgerId}")
    public ResponseEntity<?> getLedgerById(@PathVariable String ledgerId) {
        return escrowService.fetchLedgerById(ledgerId);
    }

    @DeleteMapping("/{ledgerId}")
    public ResponseEntity<?> deleteLedgerById(@PathVariable String ledgerId) {
        return escrowService.deleteLedgerById(ledgerId);
    }

    @PutMapping("/{ledgerId}/status")
    public ResponseEntity<?> updateLedgerStatus(
            @PathVariable String ledgerId) {
        return escrowService.updateLedgerStatusById(ledgerId);
    }

}