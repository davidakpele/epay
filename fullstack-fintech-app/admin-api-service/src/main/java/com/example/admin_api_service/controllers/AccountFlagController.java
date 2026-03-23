package com.example.admin_api_service.controllers;

import com.example.admin_api_service.Interfaces.IAccountFlagService;
import com.example.admin_api_service.dto.ApiResponse;
import com.example.admin_api_service.enums.AccountFlagSeverity;
import com.example.admin_api_service.enums.AccountFlagStatus;
import com.example.admin_api_service.enums.AccountFlagType;
import com.example.admin_api_service.models.userAndWalletManagement.AccountFlag;
import com.example.admin_api_service.payloads.AssignFlagPayload;
import com.example.admin_api_service.payloads.RaiseManualFlagPayload;
import com.example.admin_api_service.payloads.ResolveFlagPayload;

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
@RequestMapping("/admin/account-flags")
public class AccountFlagController {

    private final IAccountFlagService flagService;

    public AccountFlagController(IAccountFlagService flagService) {
        this.flagService = flagService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AccountFlag>> raiseManualFlag(
            @Valid @RequestBody RaiseManualFlagPayload payload,
            @AuthenticationPrincipal String adminId,
            HttpServletRequest request) {

        AccountFlag flag = flagService.raiseManualFlag(
                payload.getUserId(),
                payload.getWalletId(),
                payload.getFlagType(),
                payload.getSeverity(),
                payload.getTitle(),
                payload.getDescription(),
                adminId,
                extractClientIp(request)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(flag, "Flag raised successfully"));
    }


    @GetMapping
    public ResponseEntity<ApiResponse<Page<AccountFlag>>> getAllFlags(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) AccountFlagStatus status,
            @RequestParam(required = false) AccountFlagType type,
            @RequestParam(required = false) AccountFlagSeverity severity) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdOn").descending());
        Page<AccountFlag> flags;

        if (status != null) {
            flags = flagService.getFlagsByStatus(status, pageable);
        } else if (type != null) {
            // getFlagsByType does not exist on the interface — fall through to getAll
            // and let the client filter, or add getFlagsByType to the interface later
            flags = flagService.getAllFlags(pageable);
        } else if (severity != null) {
            flags = flagService.getFlagsBySeverity(severity, pageable);
        } else {
            flags = flagService.getAllFlags(pageable);
        }

        return ResponseEntity.ok(ApiResponse.success(flags));
    }

    @GetMapping("/{flagId}")
    public ResponseEntity<ApiResponse<AccountFlag>> getFlagById(@PathVariable String flagId) {
        return ResponseEntity.ok(ApiResponse.success(flagService.getFlagById(flagId)));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<List<AccountFlag>>> getFlagsByUser(
            @PathVariable Long userId) {

        return ResponseEntity.ok(ApiResponse.success(
                flagService.getOpenFlagsForUser(userId)));
    }

    @GetMapping("/users/{userId}/open")
    public ResponseEntity<ApiResponse<java.util.List<AccountFlag>>> getOpenFlagsByUser(
            @PathVariable Long userId) {

        // Interface returns List — not paginated
        return ResponseEntity.ok(ApiResponse.success(
                flagService.getOpenFlagsForUser(userId)));
    }

    @GetMapping("/users/{userId}/has-open")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> hasOpenFlag(
            @PathVariable Long userId,
            @RequestParam AccountFlagType flagType) {  // flagType required by interface

        return ResponseEntity.ok(ApiResponse.success(
                Map.of("hasOpenFlag", flagService.hasOpenFlag(userId, flagType))));
    }

    @PatchMapping("/{flagId}/assign")
    public ResponseEntity<ApiResponse<AccountFlag>> assignFlag(
            @PathVariable String flagId,
            @Valid @RequestBody AssignFlagPayload payload) {  // adminId removed — not in interface

        return ResponseEntity.ok(ApiResponse.success(
                flagService.assignFlag(flagId, payload.getAssignedTo()),
                "Flag assigned"));
    }

    @PatchMapping("/{flagId}/escalate")
    public ResponseEntity<ApiResponse<AccountFlag>> escalateFlag(
            @PathVariable String flagId,
            @AuthenticationPrincipal String adminId) {

        return ResponseEntity.ok(ApiResponse.success(
                flagService.escalateFlag(flagId, adminId),
                "Flag escalated"));
    }

    @PatchMapping("/{flagId}/resolve")
    public ResponseEntity<ApiResponse<AccountFlag>> resolveFlag(
            @PathVariable String flagId,
            @Valid @RequestBody ResolveFlagPayload payload,
            @AuthenticationPrincipal String adminId) {

        return ResponseEntity.ok(ApiResponse.success(
                flagService.resolveFlag(flagId, adminId, payload.getResolutionNote()),
                "Flag resolved"));
    }

    @PatchMapping("/{flagId}/false-positive")
    public ResponseEntity<ApiResponse<AccountFlag>> markFalsePositive(
            @PathVariable String flagId,
            @Valid @RequestBody ResolveFlagPayload payload,
            @AuthenticationPrincipal String adminId) {

        return ResponseEntity.ok(ApiResponse.success(
                flagService.markFalsePositive(flagId, adminId, payload.getResolutionNote()),
                "Flag marked as false positive"));
    }

    @PatchMapping("/{flagId}/link-freeze/{freezeId}")
    public ResponseEntity<ApiResponse<AccountFlag>> linkToFreeze(
            @PathVariable String flagId,
            @PathVariable String freezeId) {

        return ResponseEntity.ok(ApiResponse.success(
                flagService.linkToFreeze(flagId, freezeId),
                "Flag linked to wallet freeze"));
    }

    @PatchMapping("/{flagId}/link-restriction/{restrictionId}")
    public ResponseEntity<ApiResponse<AccountFlag>> linkToRestriction(
            @PathVariable String flagId,
            @PathVariable String restrictionId) {

        return ResponseEntity.ok(ApiResponse.success(
                flagService.linkToRestriction(flagId, restrictionId),
                "Flag linked to account restriction"));
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            // X-Forwarded-For can be a comma-separated chain — first entry is the origin
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}