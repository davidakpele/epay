package com.example.admin_api_service.controllers;

import com.example.admin_api_service.Interfaces.IUserKycService;
import com.example.admin_api_service.dto.ApiResponse;
import com.example.admin_api_service.enums.KycStatus;
import com.example.admin_api_service.enums.KycTier;
import com.example.admin_api_service.models.userAndWalletManagement.UserKyc;
import com.example.admin_api_service.payloads.BvnVerificationPayload;
import com.example.admin_api_service.payloads.KycApprovalPayload;
import com.example.admin_api_service.payloads.KycRejectionPayload;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/kyc")
public class UserKycController {

    private final IUserKycService kycService;

    public UserKycController(IUserKycService kycService) {
        this.kycService = kycService;
    }

    @PostMapping("/users/{userId}/initiate")
    public ResponseEntity<ApiResponse<UserKyc>> initiateKyc(
            @PathVariable Long userId,
            @AuthenticationPrincipal String adminId) {

        UserKyc kyc = kycService.initiateKyc(userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(kyc, "KYC initiated"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserKyc>>> getAllKyc(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) KycStatus status,
            @RequestParam(required = false) KycTier tier) {

        Page<UserKyc> result;
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdOn").descending());

        if (status != null) {
            result = kycService.getKycByStatus(status, pageable);
        } else if (tier != null) {
            result = kycService.getKycByTier(tier, pageable);
        } else {
            result = kycService.getAllKyc(pageable);
        }

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{kycId}")
    public ResponseEntity<ApiResponse<UserKyc>> getKycById(@PathVariable String kycId) {
        return ResponseEntity.ok(ApiResponse.success(kycService.getKycById(kycId)));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<UserKyc>> getKycByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(kycService.getKycByUserId(userId)));
    }

    @PatchMapping("/{kycId}/submit")
    public ResponseEntity<ApiResponse<UserKyc>> submitKyc(
            @PathVariable String kycId,
            @AuthenticationPrincipal String adminId) {

        return ResponseEntity.ok(ApiResponse.success(
                kycService.submitKyc(kycId), "KYC submitted for review"));
    }

    @PatchMapping("/{kycId}/approve")
    public ResponseEntity<ApiResponse<UserKyc>> approveKyc(
            @PathVariable String kycId,
            @Valid @RequestBody KycApprovalPayload payload,
            @AuthenticationPrincipal String adminId) {

        return ResponseEntity.ok(ApiResponse.success(
                kycService.approveKyc(kycId, payload.getGrantedTier(),
                        adminId, payload.getReviewNote()),
                "KYC approved"));
    }

    @PatchMapping("/{kycId}/reject")
    public ResponseEntity<ApiResponse<UserKyc>> rejectKyc(
            @PathVariable String kycId,
            @Valid @RequestBody KycRejectionPayload payload,
            @AuthenticationPrincipal String adminId) {

        return ResponseEntity.ok(ApiResponse.success(
                kycService.rejectKyc(kycId, adminId, payload.getRejectionReason()),
                "KYC rejected"));
    }

    @PatchMapping("/{kycId}/suspend")
    public ResponseEntity<ApiResponse<UserKyc>> suspendKyc(
            @PathVariable String kycId,
            @Valid @RequestBody KycRejectionPayload payload,
            @AuthenticationPrincipal String adminId) {

        return ResponseEntity.ok(ApiResponse.success(
                kycService.suspendKyc(kycId, adminId, payload.getRejectionReason()),
                "KYC suspended"));
    }

    @PatchMapping("/{kycId}/verify-bvn")
    public ResponseEntity<ApiResponse<UserKyc>> verifyBvn(
            @PathVariable String kycId,
            @Valid @RequestBody BvnVerificationPayload payload,
            @AuthenticationPrincipal String adminId) {

        return ResponseEntity.ok(ApiResponse.success(
                kycService.verifyBvn(kycId, payload.getBvn(), adminId),
                "BVN verified successfully"));
    }

    @GetMapping("/users/{userId}/has-open")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> hasOpenKyc(
            @PathVariable Long userId) {

        boolean hasOpen = kycService.hasOpenKyc(userId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("hasOpenKyc", hasOpen)));
    }
}