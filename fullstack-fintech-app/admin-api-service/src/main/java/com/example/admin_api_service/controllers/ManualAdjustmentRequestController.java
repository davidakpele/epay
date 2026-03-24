package com.example.admin_api_service.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.example.admin_api_service.Interfaces.IManualAdjustmentRequestService;
import com.example.admin_api_service.dto.ApiResponse;
import com.example.admin_api_service.enums.ManualAdjustmentStatus;
import com.example.admin_api_service.models.accessAndApprovals.ManualAdjustmentRequest;
import com.example.admin_api_service.payloads.MarkExecutedRequest;
import com.example.admin_api_service.payloads.ReasonRequest;

@RestController
@RequestMapping("/manual-adjustments")
public class ManualAdjustmentRequestController {

    private final IManualAdjustmentRequestService adjustmentService;

    public ManualAdjustmentRequestController(IManualAdjustmentRequestService adjustmentService) {
        this.adjustmentService = adjustmentService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ManualAdjustmentRequest>> submitAdjustment(
            @Valid @RequestBody ManualAdjustmentRequest request,
            @AuthenticationPrincipal String adminId,
            HttpServletRequest httpRequest) {

        var created = adjustmentService.submitAdjustment(
                request.getWalletId(),
                request.getUserId(),
                request.getAdjustmentType(),
                request.getAmount(),
                request.getCurrency(),
                request.getBalanceBefore(),
                request.getReason(),
                request.getInternalNote(),
                adminId,
                httpRequest.getRemoteAddr()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Adjustment request submitted"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ManualAdjustmentRequest>>> getAllAdjustments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) ManualAdjustmentStatus status) {

        Page<ManualAdjustmentRequest> adjustments =
                status != null
                ? adjustmentService.getAdjustmentsByStatus(
                        status, PageRequest.of(page, size, Sort.by("createdOn").descending()))
                : adjustmentService.getAllAdjustments(
                        PageRequest.of(page, size, Sort.by("createdOn").descending()));

        return ResponseEntity.ok(ApiResponse.success(adjustments));
    }

    @GetMapping("/{adjustmentId}")
    public ResponseEntity<ApiResponse<ManualAdjustmentRequest>> getAdjustmentById(
            @PathVariable String adjustmentId) {
        return ResponseEntity.ok(ApiResponse.success(
                adjustmentService.getAdjustmentById(adjustmentId)));
    }

    @GetMapping("/wallet/{walletId}")
    public ResponseEntity<ApiResponse<Page<ManualAdjustmentRequest>>> getByWallet(
            @PathVariable Long walletId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                adjustmentService.getAdjustmentsByWallet(
                        walletId, PageRequest.of(page, size, Sort.by("createdOn").descending()))));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Page<ManualAdjustmentRequest>>> getByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(ApiResponse.success(
                adjustmentService.getAdjustmentsByUser(
                        userId, PageRequest.of(page, size, Sort.by("createdOn").descending()))));
    }

    @PatchMapping("/{adjustmentId}/approve")
    public ResponseEntity<ApiResponse<ManualAdjustmentRequest>> approve(
            @PathVariable String adjustmentId,
            @AuthenticationPrincipal String adminId) {

        return ResponseEntity.ok(ApiResponse.success(
                adjustmentService.approveAdjustment(adjustmentId, adminId),
                "Adjustment approved"));
    }

    @PatchMapping("/{adjustmentId}/reject")
    public ResponseEntity<ApiResponse<ManualAdjustmentRequest>> reject(
            @PathVariable String adjustmentId,
            @Valid @RequestBody ReasonRequest request,
            @AuthenticationPrincipal String adminId) {
        return ResponseEntity.ok(ApiResponse.success(
                adjustmentService.rejectAdjustment(adjustmentId, adminId, request.getReason()),
                "Adjustment rejected"));
    }

    @PatchMapping("/{adjustmentId}/execute")
    public ResponseEntity<ApiResponse<ManualAdjustmentRequest>> markExecuted(
            @PathVariable String adjustmentId,
            @Valid @RequestBody MarkExecutedRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                adjustmentService.markExecuted(
                        adjustmentId, request.getBalanceAfter(), request.getHistoryReferenceId()),
                "Adjustment executed"));
    }

    @PatchMapping("/{adjustmentId}/fail")
    public ResponseEntity<ApiResponse<ManualAdjustmentRequest>> markFailed(
            @PathVariable String adjustmentId,
            @Valid @RequestBody ReasonRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                adjustmentService.markFailed(adjustmentId, request.getReason()),
                "Adjustment marked as failed"));
    }

    @PatchMapping("/{adjustmentId}/cancel")
    public ResponseEntity<ApiResponse<ManualAdjustmentRequest>> cancel(
            @PathVariable String adjustmentId,
            @AuthenticationPrincipal String adminId) {

        return ResponseEntity.ok(ApiResponse.success(
                adjustmentService.cancelAdjustment(adjustmentId, adminId),
                "Adjustment cancelled"));
    }

    
}