package com.example.admin_api_service.controllers;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.example.admin_api_service.Interfaces.IAdminTwoFactorAuthService;
import com.example.admin_api_service.dto.ApiResponse;
import com.example.admin_api_service.models.accessAndSecurity.AdminTwoFactorAuth;
import com.example.admin_api_service.payloads.TwoFactorDisableRequest;
import com.example.admin_api_service.payloads.TwoFactorInitiateRequest;
import com.example.admin_api_service.payloads.TwoFactorVerifyRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/2fa")
public class AdminTwoFactorAuthController {

    private final IAdminTwoFactorAuthService twoFactorAuthService;

    public AdminTwoFactorAuthController(IAdminTwoFactorAuthService twoFactorAuthService) {
        this.twoFactorAuthService = twoFactorAuthService;
    }

    @PostMapping("/{adminUserId}/initiate")
    public ResponseEntity<ApiResponse<AdminTwoFactorAuth>> initiate(
            @PathVariable String adminUserId,
            @Valid @RequestBody TwoFactorInitiateRequest request) {

        AdminTwoFactorAuth twoFA = twoFactorAuthService.initiate2FA(
                adminUserId, request.getMethod());
        return ResponseEntity.ok(ApiResponse.success(twoFA,
                "2FA initiated. Use the secret to set up your authenticator app."));
    }

    @PostMapping("/{adminUserId}/verify")
    public ResponseEntity<ApiResponse<AdminTwoFactorAuth>> verify(
            @PathVariable String adminUserId,
            @Valid @RequestBody TwoFactorVerifyRequest request) {

        AdminTwoFactorAuth twoFA = twoFactorAuthService.verify2FA(
                adminUserId, request.getMethod(), request.getCode());
        return ResponseEntity.ok(ApiResponse.success(twoFA, "2FA verified successfully"));
    }

    @PostMapping("/{adminUserId}/enable")
    public ResponseEntity<ApiResponse<Void>> enable(
            @PathVariable String adminUserId,
            @Valid @RequestBody TwoFactorInitiateRequest request) {

        twoFactorAuthService.enable2FA(adminUserId, request.getMethod());
        return ResponseEntity.ok(ApiResponse.success(null, "2FA enabled successfully"));
    }

    @PostMapping("/{adminUserId}/disable")
    public ResponseEntity<ApiResponse<Void>> disable(
            @PathVariable String adminUserId,
            @Valid @RequestBody TwoFactorDisableRequest request,
            @AuthenticationPrincipal String adminId) {

        twoFactorAuthService.disable2FA(adminUserId, request.getMethod(), adminId);
        return ResponseEntity.ok(ApiResponse.success(null, "2FA disabled successfully"));
    }

    @GetMapping("/{adminUserId}/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatus(
            @PathVariable String adminUserId) {

        boolean enabled = twoFactorAuthService.is2FAEnabled(adminUserId);
        boolean locked = twoFactorAuthService.is2FALocked(adminUserId);
        List<AdminTwoFactorAuth> methods = twoFactorAuthService.getAll2FAMethods(adminUserId);

        Map<String, Object> status = Map.of(
                "enabled", enabled,
                "locked", locked,
                "methods", methods
        );
        return ResponseEntity.ok(ApiResponse.success(status));
    }

    @PostMapping("/{adminUserId}/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateCode(
            @PathVariable String adminUserId,
            @RequestBody Map<String, String> body) {

        String code = body.get("code");
        boolean valid = twoFactorAuthService.validate2FACode(adminUserId, code);
        return ResponseEntity.ok(ApiResponse.success(valid));
    }

    @PostMapping("/{adminUserId}/validate-backup")
    public ResponseEntity<ApiResponse<Boolean>> validateBackupCode(
            @PathVariable String adminUserId,
            @RequestBody Map<String, String> body) {

        String backupCode = body.get("backupCode");
        boolean valid = twoFactorAuthService.validateBackupCode(adminUserId, backupCode);
        return ResponseEntity.ok(ApiResponse.success(valid));
    }

    @PostMapping("/{adminUserId}/backup-codes/regenerate")
    public ResponseEntity<ApiResponse<List<String>>> regenerateBackupCodes(
            @PathVariable String adminUserId) {

        List<String> codes = twoFactorAuthService.regenerateBackupCodes(adminUserId);
        return ResponseEntity.ok(ApiResponse.success(codes,
                "Backup codes regenerated. Store them securely — they will not be shown again."));
    }

    @PostMapping("/{adminUserId}/reset-attempts")
    public ResponseEntity<ApiResponse<Void>> resetFailedAttempts(
            @PathVariable String adminUserId) {

        twoFactorAuthService.resetFailedAttempts(adminUserId);
        return ResponseEntity.ok(ApiResponse.success(null, "Failed attempts reset"));
    }
}