package com.epay.deposit.controller;

import com.epay.common.exception.ApiResponse;
import com.epay.deposit.service.DepositService;
import com.epay.domain.deposit.dto.DepositDTO;
import com.epay.domain.deposit.input.InitiateDepositRequest;
import com.epay.domain.deposit.input.VerifyDepositRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
     * Creates a deposit and returns the gateway payment URL.
     * The userId is injected from the JWT via JwtAuthenticationFilter.
     */
    @PostMapping("/initiate")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<DepositDTO>> initiate(
            @Valid @RequestBody InitiateDepositRequest request,
            @RequestAttribute("userId") Long userId) {

        DepositDTO dto = depositService.initiate(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Deposit initiated. Redirect user to paymentUrl.", dto));
    }

    /**
     * POST /deposit/verify
     * Called after user returns from the payment page, or for polling status.
     */
    @PostMapping("/verify")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<DepositDTO>> verify(
            @Valid @RequestBody VerifyDepositRequest request,
            @RequestAttribute("userId") Long userId) {

        DepositDTO dto = depositService.verify(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Deposit status retrieved.", dto));
    }
}
