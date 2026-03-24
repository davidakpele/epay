package com.example.admin_api_service.controllers;

import com.example.admin_api_service.Interfaces.IAmlAlertService;
import com.example.admin_api_service.dto.ApiResponse;
import com.example.admin_api_service.enums.AmlAlertSeverity;
import com.example.admin_api_service.enums.AmlAlertStatus;
import com.example.admin_api_service.models.complianceAndRisk.AmlAlert;
import com.example.admin_api_service.payloads.RaiseAmlAlertPayload;
import com.example.admin_api_service.payloads.ReasonRequest;
import com.example.admin_api_service.payloads.ReviewAlertPayload;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/aml-alerts")
public class AmlAlertController {

    private final IAmlAlertService amlAlertService;

    public AmlAlertController(IAmlAlertService amlAlertService) {
        this.amlAlertService = amlAlertService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AmlAlert>> raiseManualAlert(
            @Valid @RequestBody RaiseAmlAlertPayload payload,
            @AuthenticationPrincipal String adminId) {

        AmlAlert alert = amlAlertService.raiseManualAlert(
                payload.getUserId(),
                payload.getWalletId(),
                payload.getTransactionId(),
                payload.getAlertType(),
                payload.getSeverity(),
                payload.getDescription(),
                payload.getAmount(),
                payload.getCurrency(),
                payload.getEvidence()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(alert, "AML alert raised"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AmlAlert>>> getAllAlerts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) AmlAlertStatus status,
            @RequestParam(required = false) AmlAlertSeverity severity) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by("triggeredAt").descending());
        Page<AmlAlert> alerts;

        if (status != null) {
            alerts = amlAlertService.getAlertsByStatus(status, pageable);
        } else if (severity != null) {
            alerts = amlAlertService.getAlertsBySeverity(severity, pageable);
        } else {
            alerts = amlAlertService.getAllAlerts(pageable);
        }

        return ResponseEntity.ok(ApiResponse.success(alerts));
    }

    @GetMapping("/{alertId}")
    public ResponseEntity<ApiResponse<AmlAlert>> getAlertById(@PathVariable String alertId) {
        return ResponseEntity.ok(ApiResponse.success(amlAlertService.getAlertById(alertId)));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<Page<AmlAlert>>> getAlertsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(ApiResponse.success(
                amlAlertService.getAlertsByUser(
                        userId, PageRequest.of(page, size, Sort.by("triggeredAt").descending()))));
    }

    @GetMapping("/users/{userId}/open")
    public ResponseEntity<ApiResponse<List<AmlAlert>>> getOpenAlertsForUser(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(
                amlAlertService.getOpenAlertsForUser(userId)));
    }

    @PatchMapping("/{alertId}/review")
    public ResponseEntity<ApiResponse<AmlAlert>> reviewAlert(
            @PathVariable String alertId,
            @Valid @RequestBody ReviewAlertPayload payload,
            @AuthenticationPrincipal String adminId) {

        return ResponseEntity.ok(ApiResponse.success(
                amlAlertService.reviewAlert(alertId, adminId, payload.getReviewNote()),
                "Alert marked as under review"));
    }

    @PatchMapping("/{alertId}/escalate-to-case")
    public ResponseEntity<ApiResponse<AmlAlert>> escalateToCase(
            @PathVariable String alertId,
            @AuthenticationPrincipal String adminId) {

        return ResponseEntity.ok(ApiResponse.success(
                amlAlertService.escalateToCase(alertId, adminId),
                "Alert escalated to AML case"));
    }

    @PatchMapping("/{alertId}/false-positive")
    public ResponseEntity<ApiResponse<AmlAlert>> markFalsePositive(
            @PathVariable String alertId,
            @Valid @RequestBody ReviewAlertPayload payload,
            @AuthenticationPrincipal String adminId) {

        return ResponseEntity.ok(ApiResponse.success(
                amlAlertService.markFalsePositive(alertId, adminId, payload.getReviewNote()),
                "Alert marked as false positive"));
    }

    @PatchMapping("/{alertId}/close")
    public ResponseEntity<ApiResponse<AmlAlert>> closeAlert(
            @PathVariable String alertId,
            @Valid @RequestBody ReasonRequest request,
            @AuthenticationPrincipal String adminId) {

        return ResponseEntity.ok(ApiResponse.success(
                amlAlertService.closeAlert(alertId, adminId, request.getReason()),
                "Alert closed"));
    }
}