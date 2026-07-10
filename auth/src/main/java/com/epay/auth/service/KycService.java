package com.epay.auth.service;

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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KycService {

    private final KycDocumentRepository     kycDocumentRepository;
    private final KycVerificationRepository kycVerificationRepository;
    private final UserRepository            userRepository;

    // =========================================================================
    // User-facing
    // =========================================================================

    @Transactional
    public KycDocument uploadDocument(Long userId, KycDocumentType documentType,
                                       String originalFilename) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String storageRef = "uploads/kyc/" + userId + "/" + documentType.name()
                + "_" + System.currentTimeMillis() + "_" + originalFilename;

        KycDocument doc = KycDocument.builder()
                .user(user)
                .documentType(documentType)
                .status(KycStatus.SUBMITTED)
                .storageReference(storageRef)
                .uploadedAt(LocalDateTime.now())
                .build();

        kycDocumentRepository.save(doc);
        log.info("[KYC] Document uploaded: userId={} type={}", userId, documentType);
        return doc;
    }

    public List<KycDocument> getDocuments(Long userId) {
        return kycDocumentRepository.findByUserId(userId);
    }

    @Transactional
    public void submitForReview(Long userId, KycTier tier) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<KycDocument> docs = kycDocumentRepository.findByUserId(userId);
        if (docs.isEmpty())
            throw new BadRequestException("Upload at least one document before submitting",
                    ErrorCode.INVALID_INPUT);

        // Check if already under review
        if (kycVerificationRepository.existsByUserIdAndTierAndStatus(
                userId, tier, KycStatus.SUBMITTED))
            throw new BadRequestException("A submission for this tier is already under review",
                    ErrorCode.RESOURCE_ALREADY_EXISTS);

        KycVerification verification = KycVerification.builder()
                .user(user)
                .tier(tier)
                .status(KycStatus.SUBMITTED)
                .submittedByUserId(userId)
                .submittedAt(LocalDateTime.now())
                .attemptCount(1)
                .build();

        kycVerificationRepository.save(verification);
        userRepository.updateKycStatus(userId, KycStatus.SUBMITTED);

        log.info("[KYC] Submitted for review: userId={} tier={}", userId, tier);
    }

    public KycStatus getStatus(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"))
                .getKycStatus();
    }

    // =========================================================================
    // Admin/Compliance-facing
    // =========================================================================

    public Page<KycVerification> getPendingReviews(Pageable pageable) {
        return kycVerificationRepository.findByStatus(KycStatus.SUBMITTED, pageable);
    }

    @Transactional
    public void approve(Long verificationId, Long reviewerId, String internalNote) {
        KycVerification v = kycVerificationRepository.findById(verificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Verification not found"));

        if (v.getStatus() != KycStatus.SUBMITTED && v.getStatus() != KycStatus.UNDER_REVIEW)
            throw new BadRequestException("Verification is not in a reviewable state",
                    ErrorCode.OPERATION_NOT_ALLOWED);

        kycVerificationRepository.updateReviewDecision(
                verificationId, KycStatus.APPROVED, reviewerId,
                LocalDateTime.now(), null, internalNote);

        userRepository.updateKycTierAndStatus(v.getUser().getId(), v.getTier(), KycStatus.APPROVED);

        log.info("[KYC] Approved: verificationId={} by reviewerId={}", verificationId, reviewerId);
    }

    @Transactional
    public void reject(Long verificationId, Long reviewerId,
                        String rejectionReason, String internalNote) {
        KycVerification v = kycVerificationRepository.findById(verificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Verification not found"));

        kycVerificationRepository.updateReviewDecision(
                verificationId, KycStatus.REJECTED, reviewerId,
                LocalDateTime.now(), rejectionReason, internalNote);

        userRepository.updateKycStatus(v.getUser().getId(), KycStatus.REJECTED);

        log.info("[KYC] Rejected: verificationId={} reason={}", verificationId, rejectionReason);
    }
}
