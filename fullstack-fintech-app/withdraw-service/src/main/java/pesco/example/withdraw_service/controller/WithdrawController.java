package pesco.example.withdraw_service.controller;

import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import pesco.example.withdraw_service.dtos.DeductWalletRequestDTO;
import pesco.example.withdraw_service.dtos.TransferWalletRequestDTO;
import pesco.example.withdraw_service.exceptions.Error;
import pesco.example.withdraw_service.services.WalletService;
import pesco.example.withdraw_service.utils.RateLimit;

@RestController
@Validated
@Slf4j
@RequestMapping("/api/withdrawals")
public class WithdrawController {
  
    private final WalletService walletService;
    private final  HttpServletRequest httpServletRequest;


    public WithdrawController(WalletService walletService, HttpServletRequest httpServletRequest) {
        this.walletService = walletService;
        this.httpServletRequest = httpServletRequest;
    }


    @RateLimit(limit = 20, duration = 300) 
    @PostMapping("/user")
    public ResponseEntity<?> transferToUser(
            @Valid @RequestBody DeductWalletRequestDTO dto, 
            @RequestHeader("Authorization") String authorizationHeader,
            Authentication authentication) {

        String token = authorizationHeader.replace("Bearer ", "");
        if (token.isBlank() || token.isEmpty()) {
            return Error.createResponse("UNAUTHORIZED*.",
                    HttpStatus.UNAUTHORIZED, "Require token to access this endpoint, Missing valid token.");
        }
        String extractedUser = authentication.getName();
        if (!extractedUser.equals(dto.getUsername())) {
            return Error.createResponse("Access Denied", HttpStatus.FORBIDDEN,
                    "You are not authorized to operate this wallet.");
        }
        return walletService.processWithdraw(dto, token, httpServletRequest);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'CUSTOMER-SERVICE')")
    @RateLimit(limit = 10, duration = 300)
    @PostMapping("/bank")
    public ResponseEntity<?> withdrawToBank(@Valid @RequestBody TransferWalletRequestDTO request, @RequestHeader("Authorization") String authorizationHeader, Authentication authentication,   @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        String token = authorizationHeader.replace("Bearer ", "");
        if (token.isBlank() || token.isEmpty()) {
            return Error.createResponse("UNAUTHORIZED*.",
                    HttpStatus.UNAUTHORIZED, "Require token to access this endpoint, Missing valid token.");
        }
        String extractedUser = authentication.getName();
        if (!extractedUser.equals(request.getUsername())) {
            return Error.createResponse("Access Denied", HttpStatus.FORBIDDEN,
                    "You are not authorized to operate this wallet.");
        }
        return walletService.processTransfer(request, token);
    }
}
