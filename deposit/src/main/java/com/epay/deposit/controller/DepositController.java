package com.epay.deposit.controller;

import com.epay.common.exception.ApiResponse;
import com.epay.deposit.service.DepositService;
import com.epay.domain.deposit.dto.DepositDTO;
import com.epay.domain.deposit.input.InitiateDepositRequest;
import com.epay.domain.deposit.input.VerifyDepositRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/deposit")
@RequiredArgsConstructor
public class DepositController {

    private final DepositService depositService;

    /**
     * POST /deposit/initiate
     * Validates user, wallet, currency — then credits wallet and records ledger.
     */
    @PostMapping("/initiate")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> initiate(
            @Valid @RequestBody InitiateDepositRequest request,
            @RequestAttribute("userId") Long userId,
            HttpServletRequest httpRequest) {

        // Inject request context into payload for audit trail
        request.setUserId(userId);
        if (request.getIpAddress() == null) request.setIpAddress(extractIp(httpRequest));
        if (request.getUserAgent() == null) request.setUserAgent(httpRequest.getHeader("User-Agent"));

        return depositService.createDeposit(request);
    }

    /**
     * POST /deposit/verify
     * Called after returning from gateway payment page, or for polling status.
     */
    @PostMapping("/verify")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<DepositDTO>> verify(
            @Valid @RequestBody VerifyDepositRequest request,
            @RequestAttribute("userId") Long userId) {

        DepositDTO dto = depositService.verify(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Deposit status retrieved.", dto));
    }

    private String extractIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
        return request.getRemoteAddr();
    }
}
