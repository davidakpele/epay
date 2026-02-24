package pesco.deposit_service.services;

import org.springframework.http.ResponseEntity;

import pesco.deposit_service.payloads.DepositRequest;

public interface DepositService {
    ResponseEntity<?> createDeposit(DepositRequest request, String token);
}
