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

    @PostMapping("/initiate")
    @PreAuthorize("hasRole('USER') and @security.hasPermission('payment:create') and @security.hasValidSession()")
    public ResponseEntity<?> initiate(
            @Valid @RequestBody InitiateDepositRequest request,
            HttpServletRequest httpRequest) {

        if (request.getIpAddress() == null)
            request.setIpAddress(extractIp(httpRequest));

        if (request.getUserAgent() == null)
            request.setUserAgent(httpRequest.getHeader("User-Agent"));

        return depositService.createDeposit(request);
    }

    @PostMapping("/verify")
    @PreAuthorize("hasRole('USER') and @security.hasPermission('payment:read') and @security.hasValidSession()")
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
