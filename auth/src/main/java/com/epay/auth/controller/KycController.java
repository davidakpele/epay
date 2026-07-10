package com.epay.auth.controller;

import com.epay.common.exception.ApiResponse;
import com.epay.common.exception.BadRequestException;
import com.epay.common.exception.ErrorCode;
import com.epay.common.exception.ResourceNotFoundException;
import com.epay.domain.auth.entity.KycDocument;
import com.epay.domain.auth.entity.KycVerification;
import com.epay.domain.auth.entity.User;
import com.epay.domain.auth.enums.KycDocumentType;
import com.epay.domain.auth.enums.KycStatus;
import com.epay.domain.auth.enums.KycTier;
import com.epay.domain.auth.repository.KycDocumentRepository;
import com.epay.domain.auth.repository.KycVerificationRepository;
import com.epay.domain.auth.repository.UserRepository;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/kyc")
@RequiredArgsConstructor
public class KycController {

    private final KycDocumentRepository      kycDocumentRepository;
    private final KycVerificationRepository  kycVerificationRepository;
    private final UserRepository             userRepository;

    // ── User: upload document ─────────────────────────────────────────────────

    /**
     * POST /kyc/documents
     * User uploads a KYC document.
     * Real file storage (S3 etc.) would handle the bytes — here we record the reference.
     */
    @PostMapping("/documents")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> uploadDocument(
            @RequestAttribute("userId") Long userId,
            @RequestParam KycDocumentType documentType,
            @RequestParam MultipartFile file) {

        if (file == null || file.isEmpty())
            throw new BadRequestException("Document file is required", ErrorCode.INVALID_INPUT);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Store a reference — in production, upload to S3 and store the key
        String storageRef = "uploads/kyc/" + userId + "/" + documentType.name() + "_"
                + System.currentTimeMillis() + "_" + file.getOriginalFilename();

        KycDocument doc = KycDocument.builder()
                .user(user)
                .documentType(documentType)
                .status(KycStatus.SUBMITTED)
                .storageReference(storageRef)
                .uploadedAt(LocalDateTime.now())
                .build();

        kycDocumentRepository.save(doc);
        return ResponseEntity.ok(ApiResponse.success("Document uploaded successfully.", null));
    }

    /** GET /kyc/documents — list user's KYC documents */
    @GetMapping("/documents")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<KycDocument>>> getDocuments(
            @RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(ApiResponse.success(null,
                kycDocumentRepository.findByUserId(userId)));
    }

    /** POST /kyc/submit/{tier} — submit for KYC review */
    @PostMapping("/submit/{tier}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> submitForReview(
            @RequestAttribute("userId") Long userId,
            @PathVariable KycTier tier) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if there are documents uploaded
        List<KycDocument> docs = kycDocumentRepository.findByUserId(userId);
        if (docs.isEmpty())
            throw new BadRequestException("Please upload at least one document before submitting",
                    ErrorCode.INVALID_INPUT);

        KycVerification verification = KycVerification.builder()
                .user(user)
                .tier(tier)
                .status(KycStatus.SUBMITTED)
                .submittedByUserId(userId)
                .submittedAt(LocalDateTime.now())
                .attemptCount(1)
                .build();

        kycVerificationRepository.save(verification);

        // Update user KYC status
        userRepository.updateKycStatus(userId, KycStatus.SUBMITTED);

        return ResponseEntity.ok(ApiResponse.success("KYC submission received. Under review.", null));
    }

    // ── Admin/Compliance: review documents ────────────────────────────────────

    /** GET /kyc/admin/pending — pending KYC submissions */
    @GetMapping("/admin/pending")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER')")
    public ResponseEntity<ApiResponse<Page<KycVerification>>> getPending(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<KycVerification> page = kycVerificationRepository.findByStatus(
                KycStatus.SUBMITTED, pageable);
        return ResponseEntity.ok(ApiResponse.success(null, page));
    }

    /** POST /kyc/admin/{verificationId}/approve */
    @PostMapping("/admin/{verificationId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER')")
    public ResponseEntity<ApiResponse<Void>> approve(
            @PathVariable Long verificationId,
            @RequestAttribute("userId") Long reviewerId,
            @RequestParam(required = false) String internalNote) {

        KycVerification v = kycVerificationRepository.findById(verificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Verification not found"));

        kycVerificationRepository.updateReviewDecision(
                verificationId, KycStatus.APPROVED, reviewerId,
                LocalDateTime.now(), null, internalNote);

        // Promote user tier
        userRepository.updateKycTierAndStatus(v.getUser().getId(), v.getTier(), KycStatus.APPROVED);

        return ResponseEntity.ok(ApiResponse.success("KYC approved.", null));
    }

    /** POST /kyc/admin/{verificationId}/reject */
    @PostMapping("/admin/{verificationId}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER')")
    public ResponseEntity<ApiResponse<Void>> reject(
            @PathVariable Long verificationId,
            @RequestAttribute("userId") Long reviewerId,
            @RequestParam @NotBlank String rejectionReason,
            @RequestParam(required = false) String internalNote) {

        KycVerification v = kycVerificationRepository.findById(verificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Verification not found"));

        kycVerificationRepository.updateReviewDecision(
                verificationId, KycStatus.REJECTED, reviewerId,
                LocalDateTime.now(), rejectionReason, internalNote);

        userRepository.updateKycStatus(v.getUser().getId(), KycStatus.REJECTED);

        return ResponseEntity.ok(ApiResponse.success("KYC rejected.", null));
    }

    /** GET /kyc/status — get the authenticated user's KYC status */
    @GetMapping("/status")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Object>> getStatus(
            @RequestAttribute("userId") Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        java.util.Map<String, Object> status = new java.util.LinkedHashMap<>();
        status.put("kycStatus", user.getKycStatus());
        status.put("kycTier",   user.getKycTier());
        return ResponseEntity.ok(ApiResponse.success(null, status));
    }
}
