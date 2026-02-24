package pesco.example.withdraw_service.services;

import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;
import pesco.example.withdraw_service.dtos.DeductWalletRequestDTO;
import pesco.example.withdraw_service.dtos.TransferWalletRequestDTO;

public interface WalletService {

    ResponseEntity<?> processTransfer(TransferWalletRequestDTO request, String token);

    public ResponseEntity<?> processWithdraw(DeductWalletRequestDTO dto, String token, HttpServletRequest httpServletRequest);

}
