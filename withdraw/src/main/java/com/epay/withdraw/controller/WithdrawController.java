package com.epay.withdraw.controller;

import com.epay.domain.withdraw.input.WithdrawRequest;
import com.epay.withdraw.service.WithdrawService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/withdrawals")
@RequiredArgsConstructor
public class WithdrawController {

    private final WithdrawService withdrawService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> withdraw(
            @Valid @RequestBody WithdrawRequest request,
            HttpServletRequest httpRequest) {

        if (request.getIpAddress() == null)
            request.setIpAddress(extractIp(httpRequest));
        if (request.getUserAgent() == null)
            request.setUserAgent(httpRequest.getHeader("User-Agent"));

        return withdrawService.withdraw(request.getUserId(), request);
    }

    private String extractIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
        return request.getRemoteAddr();
    }
}
