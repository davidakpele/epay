package com.example.admin_api_service.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.example.admin_api_service.Interfaces.IAdminPasswordHistoryService;
import com.example.admin_api_service.dto.ApiResponse;
import com.example.admin_api_service.models.accessAndSecurity.AdminPasswordHistory;
import com.example.admin_api_service.payloads.PasswordChangeRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/password")
public class AdminPasswordHistoryController {

    private static final int HISTORY_DEPTH = 5;
    private static final int MAX_PASSWORD_AGE_DAYS = 90;

    private final IAdminPasswordHistoryService passwordHistoryService;
    private final PasswordEncoder passwordEncoder;

    public AdminPasswordHistoryController(IAdminPasswordHistoryService passwordHistoryService,
                                           PasswordEncoder passwordEncoder) {
        this.passwordHistoryService = passwordHistoryService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/{adminUserId}/change")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @PathVariable Long adminUserId,
            @Valid @RequestBody PasswordChangeRequest request,
            @AuthenticationPrincipal String requestingAdminId,
            HttpServletRequest httpRequest) {

        // Check if new password was recently used
        if (passwordHistoryService.isPasswordReused(
                adminUserId, request.getNewPassword(), HISTORY_DEPTH)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Password was recently used. Choose a different password."));
        }

        String newHash = passwordEncoder.encode(request.getNewPassword());
        passwordHistoryService.recordPasswordChange(
                adminUserId, newHash,
                "VOLUNTARY_CHANGE",
                requestingAdminId,
                httpRequest.getRemoteAddr(),
                httpRequest.getHeader("User-Agent")
        );

        return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully"));
    }

    @GetMapping("/{adminUserId}/history")
    public ResponseEntity<ApiResponse<List<AdminPasswordHistory>>> getPasswordHistory(
            @PathVariable Long adminUserId) {

        List<AdminPasswordHistory> history =
                passwordHistoryService.getPasswordHistory(adminUserId);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @GetMapping("/{adminUserId}/expired")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkExpiry(
            @PathVariable Long adminUserId) {

        boolean expired = passwordHistoryService
                .isPasswordExpired(adminUserId, MAX_PASSWORD_AGE_DAYS);

        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "expired", expired,
                "maxAgeDays", MAX_PASSWORD_AGE_DAYS
        )));
    }
}
