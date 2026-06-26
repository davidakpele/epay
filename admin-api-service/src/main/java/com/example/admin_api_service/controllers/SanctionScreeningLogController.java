package com.example.admin_api_service.controllers;

import com.example.admin_api_service.Interfaces.ISanctionScreeningLogService;
import com.example.admin_api_service.dto.ApiResponse;
import com.example.admin_api_service.enums.SanctionScreeningResult;
import com.example.admin_api_service.models.complianceAndRisk.SanctionScreeningLog;
import com.example.admin_api_service.payloads.ReviewScreeningPayload;
import com.example.admin_api_service.payloads.ScreeningPayload;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sanction-screenings")
public class SanctionScreeningLogController {

    private final ISanctionScreeningLogService screeningService;

    public SanctionScreeningLogController(ISanctionScreeningLogService screeningService) {
        this.screeningService = screeningService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SanctionScreeningLog>> runScreening(
            @Valid @RequestBody ScreeningPayload payload) {

        SanctionScreeningLog log = screeningService.screen(
                payload.getUserId(),
                payload.getWalletId(),
                payload.getTransactionId(),
                payload.getTrigger(),
                payload.getScreenedName()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(log, "Screening completed"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<SanctionScreeningLog>>> getAllLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) SanctionScreeningResult result) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by("screenedAt").descending());
        Page<SanctionScreeningLog> logs = result != null
                ? screeningService.getLogsByResult(result, pageable)
                : screeningService.getAllLogs(pageable);

        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    @GetMapping("/{logId}")
    public ResponseEntity<ApiResponse<SanctionScreeningLog>> getLogById(
            @PathVariable String logId) {
        return ResponseEntity.ok(ApiResponse.success(screeningService.getLogById(logId)));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<Page<SanctionScreeningLog>>> getLogsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(ApiResponse.success(
                screeningService.getLogsByUser(
                        userId, PageRequest.of(page, size, Sort.by("screenedAt").descending()))));
    }

    @GetMapping("/transactions/{transactionId}")
    public ResponseEntity<ApiResponse<Page<SanctionScreeningLog>>> getLogsByTransaction(
            @PathVariable String transactionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(ApiResponse.success(
                screeningService.getLogsByTransaction(
                        transactionId, PageRequest.of(page, size, Sort.by("screenedAt").descending()))));
    }

    @PatchMapping("/{logId}/review")
    public ResponseEntity<ApiResponse<SanctionScreeningLog>> reviewLog(
            @PathVariable String logId,
            @Valid @RequestBody ReviewScreeningPayload payload,
            @AuthenticationPrincipal String adminId) {

        return ResponseEntity.ok(ApiResponse.success(
                screeningService.reviewLog(
                        logId, adminId, payload.getReviewNote(), payload.getOverrideResult()),
                "Screening log reviewed"));
    }

    @PatchMapping("/{logId}/escalate-to-case")
    public ResponseEntity<ApiResponse<SanctionScreeningLog>> escalateToCase(
            @PathVariable String logId,
            @AuthenticationPrincipal String adminId) {

        return ResponseEntity.ok(ApiResponse.success(
                screeningService.escalateToAmlCase(logId, adminId),
                "Screening escalated to AML case"));
    }
}