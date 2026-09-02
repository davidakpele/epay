package com.epay.withdraw.controller;

import com.epay.common.config.security.JwtClaimsHolder;
import com.epay.common.interfaces.RateLimited;
import com.epay.domain.common.exception.ErrorHandler;
import com.epay.domain.withdraw.input.BankWithdrawRequest;
import com.epay.domain.withdraw.input.InternalWithdrawRequest;
import com.epay.withdraw.service.WithdrawService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/withdrawals")
@RequiredArgsConstructor
public class WithdrawController {

    private final WithdrawService withdrawService;
    private final ErrorHandler    errorHandler;
    private final JwtClaimsHolder jwtClaims;

    @RateLimited(
        keyPrefix      = "in_house_transfer",
        capacity       = 5,
        duration       = 1,
        timeUnit       = TimeUnit.MINUTES,
        userIdentifier = "#request.userId",
        cost           = 1
    )
    @PostMapping("/user")
    @PreAuthorize("hasRole('USER') and @security.hasPermission('payment:create') and @security.hasValidSession()")
    public ResponseEntity<?> withdraw(
            @Valid @RequestBody InternalWithdrawRequest request,
            @RequestHeader(value = "User-Agent",     required = false) String userAgent,
            @RequestHeader(value = "X-Geo-Location", required = false) String geoLocation,
            @RequestHeader(value = "X-Device-Id",    required = false) String deviceId,
            HttpServletRequest httpRequest) {

        return withdrawService.internalWithdrawProcess(request);
    }

    @RateLimited(
        keyPrefix      = "bank_transfer",
        capacity       = 10,
        duration       = 1,
        timeUnit       = TimeUnit.MINUTES,
        userIdentifier = "#request.userId"
    )
    @PostMapping("/bank")
    @PreAuthorize("hasAnyRole('USER','ADMIN','CUSTOMER_SERVICE') and @security.hasPermission('payment:create') and @security.hasValidSession()")
    public ResponseEntity<?> withdrawToBank(
            @Valid @RequestBody BankWithdrawRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {

        // ── Ownership guard: the authenticated user must own the userId in the request
        // Uses JWT userId claim directly — avoids the broken auth.getName() UUID comparison.
        Long tokenUserId = jwtClaims.getUserId();
        if (tokenUserId != null && !tokenUserId.equals(request.getUserId())) {
            return errorHandler.error("FORBIDDEN", HttpStatus.FORBIDDEN,
                    "You are not authorized to operate this wallet.");
        }

        return withdrawService.bankWithdrawProcess(request);
    }
}
