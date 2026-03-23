package com.example.admin_api_service.controllers;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.example.admin_api_service.Interfaces.IApprovalRequestService;
import com.example.admin_api_service.dto.ApiResponse;
import com.example.admin_api_service.enums.ApprovalRequestStatus;
import com.example.admin_api_service.models.accessAndApprovals.ApprovalRequest;
import com.example.admin_api_service.payloads.ApprovalActionRequest;
import com.example.admin_api_service.payloads.ApprovalRequestSubmit;

@RestController
@RequestMapping("/admin/approval-requests")
public class ApprovalRequestController {

    private final IApprovalRequestService approvalRequestService;

    public ApprovalRequestController(IApprovalRequestService approvalRequestService) {
        this.approvalRequestService = approvalRequestService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ApprovalRequest>> submitRequest(
            @Valid @RequestBody ApprovalRequestSubmit request,
            @AuthenticationPrincipal String adminId) {

        ApprovalRequest created = approvalRequestService.submitRequest(
                request.getWorkflowType(),
                request.getTargetType(),
                request.getTargetId(),
                request.getAmount(),
                request.getCurrency(),
                request.getPayload(),
                adminId,
                request.getRequestNote()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Approval request submitted"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ApprovalRequest>>> getAllRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) ApprovalRequestStatus status) {

        Page<ApprovalRequest> requests = status != null
                ? approvalRequestService.getRequestsByStatus(
                        status, PageRequest.of(page, size, Sort.by("createdOn").descending()))
                : approvalRequestService.getAllRequests(
                        PageRequest.of(page, size, Sort.by("createdOn").descending()));

        return ResponseEntity.ok(ApiResponse.success(requests));
    }
    
    @GetMapping("/{requestId}")
    public ResponseEntity<ApiResponse<ApprovalRequest>> getRequestById(
            @PathVariable String requestId) {
        return ResponseEntity.ok(ApiResponse.success(
                approvalRequestService.getRequestById(requestId)));
    }

    @GetMapping("/my-requests")
    public ResponseEntity<ApiResponse<Page<ApprovalRequest>>> getMyRequests(
            @AuthenticationPrincipal String adminId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<ApprovalRequest> requests = approvalRequestService.getRequestsByRequestedBy(
                adminId, PageRequest.of(page, size, Sort.by("createdOn").descending()));
        return ResponseEntity.ok(ApiResponse.success(requests));
    }

    @GetMapping("/pending-for-me")
    public ResponseEntity<ApiResponse<Page<ApprovalRequest>>> getPendingForMe(
            @AuthenticationPrincipal String adminId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<ApprovalRequest> requests = approvalRequestService.getPendingRequestsForAdmin(
                adminId, PageRequest.of(page, size, Sort.by("createdOn").ascending()));
        return ResponseEntity.ok(ApiResponse.success(requests));
    }

    @PatchMapping("/{requestId}/approve")
    public ResponseEntity<ApiResponse<ApprovalRequest>> approveStep(
            @PathVariable String requestId,
            @Valid @RequestBody ApprovalActionRequest request,
            @AuthenticationPrincipal String adminId) {

        ApprovalRequest updated = approvalRequestService.approveStep(
                requestId, adminId, request.getNote());
        return ResponseEntity.ok(ApiResponse.success(updated, "Step approved"));
    }

    @PatchMapping("/{requestId}/reject")
    public ResponseEntity<ApiResponse<ApprovalRequest>> rejectRequest(
            @PathVariable String requestId,
            @Valid @RequestBody ApprovalActionRequest request,
            @AuthenticationPrincipal String adminId) {

        ApprovalRequest updated = approvalRequestService.rejectRequest(
                requestId, adminId, request.getNote());
        return ResponseEntity.ok(ApiResponse.success(updated, "Request rejected"));
    }

    @PatchMapping("/{requestId}/cancel")
    public ResponseEntity<ApiResponse<ApprovalRequest>> cancelRequest(
            @PathVariable String requestId,
            @Valid @RequestBody ApprovalActionRequest request,
            @AuthenticationPrincipal String adminId) {

        ApprovalRequest updated = approvalRequestService.cancelRequest(
                requestId, adminId, request.getNote());
        return ResponseEntity.ok(ApiResponse.success(updated, "Request cancelled"));
    }

    @PatchMapping("/{requestId}/escalate")
    public ResponseEntity<ApiResponse<ApprovalRequest>> escalateRequest(
            @PathVariable String requestId,
            @Valid @RequestBody ApprovalActionRequest request,
            @AuthenticationPrincipal String adminId) {

        ApprovalRequest updated = approvalRequestService.escalateRequest(
                requestId, adminId, request.getNote());
        return ResponseEntity.ok(ApiResponse.success(updated, "Request escalated"));
    }
}