package com.epay.withdraw.controller;

import com.epay.common.interfaces.RateLimited;
import com.epay.domain.common.exception.ErrorHandler;
import com.epay.domain.withdraw.input.BankWithdrawRequest;
import com.epay.domain.withdraw.input.InternalWithdrawRequest;
import com.epay.withdraw.service.WithdrawService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.concurrent.TimeUnit;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/withdrawals")
@RequiredArgsConstructor
public class WithdrawController {

    private final WithdrawService withdrawService;
    private final ErrorHandler    errorHandler;
    
    @RateLimited(
        keyPrefix = "in_house_transfer",
        capacity = 5,
        duration = 1,
        timeUnit = TimeUnit.MINUTES,
        userIdentifier = "#request.userId",
        cost = 1
    )
    @PostMapping("/user")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> withdraw(
            @Valid @RequestBody InternalWithdrawRequest request,
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestHeader(value = "User-Agent",      required = false) String userAgent,
            @RequestHeader(value = "X-Geo-Location",  required = false) String geoLocation,
            @RequestHeader(value = "X-Device-Id",     required = false) String deviceId,
            HttpServletRequest httpRequest) {

        return withdrawService.internalWithdrawProcess(request);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'CUSTOMER-SERVICE')")
    @RateLimited(
        keyPrefix = "internal_transfer",
        capacity = 10,
        duration = 1,
        timeUnit = TimeUnit.MINUTES,
        userIdentifier = "#request.userId"
    )
    @PostMapping("/bank")
    public ResponseEntity<?> withdrawToBank(@Valid @RequestBody BankWithdrawRequest request, @RequestHeader("Authorization") String authorizationHeader, Authentication authentication,   @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        String token = authorizationHeader.replace("Bearer ", "");
        if (token.isBlank() || token.isEmpty()) {
             return errorHandler.error("UNAUTHORIZED", HttpStatus.FORBIDDEN,
                    "Require token to access this endpoint, Missing valid token.");
        }
        String extractedUser = authentication.getName();
        if (!extractedUser.equals(request.getUsername())) {
            return errorHandler.error("FORBIDDEN", HttpStatus.FORBIDDEN,
                "You are not authorized to operate this wallet");
        }
        return withdrawService.bankWithdrawProcess(request);
    }

}