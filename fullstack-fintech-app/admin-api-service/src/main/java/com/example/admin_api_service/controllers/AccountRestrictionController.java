package com.example.admin_api_service.controllers;

import com.example.admin_api_service.Interfaces.IAccountRestrictionService;
import com.example.admin_api_service.dto.ApiResponse;
import com.example.admin_api_service.enums.AccountRestrictionStatus;
import com.example.admin_api_service.models.userAndWalletManagement.AccountRestriction;
import com.example.admin_api_service.payloads.AccountRestrictionPayload;
import com.example.admin_api_service.payloads.LiftRestrictionPayload;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/account-restrictions")
public class AccountRestrictionController {

    private final IAccountRestrictionService restrictionService;

    public AccountRestrictionController(IAccountRestrictionService restrictionService) {
        this.restrictionService = restrictionService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AccountRestriction>> applyRestriction(
            @Valid @RequestBody AccountRestrictionPayload payload,
            @AuthenticationPrincipal String adminId,
            HttpServletRequest request) {

        AccountRestriction restriction = restrictionService.applyRestriction(
                payload.getUserId(),
                payload.getWalletId(),
                payload.getRestrictionType(),
                payload.getReason(),
                payload.getInternalNote(),
                payload.getLimitAmount(),
                payload.getLimitCurrency(),
                payload.getExternalReference(),
                adminId,
                extractClientIp(request),      // ipAddress — resolved server-side
                payload.getExpiresAt()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(restriction, "Restriction applied successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AccountRestriction>>> getAllRestrictions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) AccountRestrictionStatus status) {

        // getRestrictionsByType() does not exist on the interface — type filter removed
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdOn").descending());
        Page<AccountRestriction> restrictions;

        if (status != null) {
            restrictions = restrictionService.getRestrictionsByStatus(status, pageable);
        } else {
            restrictions = restrictionService.getAllRestrictions(pageable);
        }

        return ResponseEntity.ok(ApiResponse.success(restrictions));
    }

    @GetMapping("/{restrictionId}")
    public ResponseEntity<ApiResponse<AccountRestriction>> getRestrictionById(
            @PathVariable String restrictionId) {
        return ResponseEntity.ok(ApiResponse.success(
                restrictionService.getRestrictionById(restrictionId)));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<List<AccountRestriction>>> getRestrictionsByUser(
            @PathVariable Long userId) {
        // No paginated version exists on the interface — returns active restrictions only
        return ResponseEntity.ok(ApiResponse.success(
                restrictionService.getActiveRestrictionsByUser(userId)));
    }

    @PatchMapping("/{restrictionId}/lift")
    public ResponseEntity<ApiResponse<AccountRestriction>> liftRestriction(
            @PathVariable String restrictionId,
            @Valid @RequestBody LiftRestrictionPayload payload,
            @AuthenticationPrincipal String adminId) {

        return ResponseEntity.ok(ApiResponse.success(
                restrictionService.liftRestriction(
                        restrictionId, adminId, payload.getLiftNote()),
                "Restriction lifted successfully"));
    }

    @GetMapping("/users/{userId}/login-disabled")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> isLoginDisabled(
            @PathVariable Long userId) {

        return ResponseEntity.ok(ApiResponse.success(
                Map.of("loginDisabled", restrictionService.isLoginDisabled(userId))));
    }

    @GetMapping("/users/{userId}/transaction-disabled")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> isTransactionDisabled(
            @PathVariable Long userId) {

        return ResponseEntity.ok(ApiResponse.success(
                Map.of("transactionDisabled", restrictionService.isTransactionDisabled(userId))));
    }

    @GetMapping("/users/{userId}/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserRestrictionStatus(
            @PathVariable Long userId) {

        List<AccountRestriction> active = restrictionService.getActiveRestrictionsByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "userId", userId,
                "loginDisabled", restrictionService.isLoginDisabled(userId),
                "transactionDisabled", restrictionService.isTransactionDisabled(userId),
                "activeRestrictionCount", active.size(),
                "activeRestrictions", active
        )));
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    /**
     * Resolves the real client IP, accounting for reverse proxies and load balancers
     * that set X-Forwarded-For or X-Real-IP headers.
     */
    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}