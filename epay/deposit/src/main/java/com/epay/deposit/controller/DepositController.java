package com.epay.deposit.controller;

import com.epay.common.exception.ApiResponse;
import com.epay.deposit.service.DepositService;
import com.epay.domain.deposit.dto.DepositDTO;
import com.epay.domain.deposit.input.InitiateDepositRequest;
import com.epay.domain.deposit.input.VerifyDepositRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Deposit", description = "Initiate and verify wallet deposit transactions via Paystack or Flutterwave")
@RestController
@RequestMapping("/deposit")
@RequiredArgsConstructor
public class DepositController {

    private final DepositService depositService;

    @Operation(
        summary     = "Initiate a deposit",
        description = "Creates a deposit session with the selected payment gateway and returns a checkout URL."
    )
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

    @Operation(
        summary     = "Verify a deposit status",
        description = "Queries the payment gateway for the current status of a deposit and credits the wallet if confirmed."
    )
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
