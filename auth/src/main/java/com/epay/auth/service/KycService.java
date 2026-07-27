package com.epay.auth.service;

import com.epay.common.config.security.FileStorageConfig;
import com.epay.common.exception.BadRequestException;
import com.epay.common.exception.ErrorCode;
import com.epay.common.exception.ResourceNotFoundException;
import com.epay.domain.auth.entity.KycDocument;
import com.epay.domain.auth.entity.KycVerification;
import com.epay.domain.auth.entity.User;
import com.epay.domain.auth.entity.UserRecord;
import com.epay.domain.auth.enums.KycDocumentType;
import com.epay.domain.auth.enums.KycStatus;
import com.epay.domain.auth.enums.KycTier;
import com.epay.domain.auth.repository.KycDocumentRepository;
import com.epay.domain.auth.repository.KycVerificationRepository;
import com.epay.domain.auth.repository.UserRecordRepository;
import com.epay.domain.auth.repository.UserRepository;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KycService {

    private final KycDocumentRepository     kycDocumentRepository;
    private final KycVerificationRepository kycVerificationRepository;
    private final UserRepository            userRepository;
    private final UserRecordRepository userRecordRepository;
    private final FileStorageConfig fileStorageConfig;

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

    public ResponseEntity<?> uploadUserProfileImage(Long id, MultipartFile image) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", "error", "message", "Unauthorized or user not found."));
        }

        Optional<UserRecord> recordOpt = userRecordRepository.findByUserId(id);
        if (recordOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", "error", "message", "User record not found."));
        }

        try {
            Path uploadDir = Paths.get(fileStorageConfig.getUploadDir()).toAbsolutePath().normalize();
            Files.createDirectories(uploadDir);
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(uploadDir, id + ".*")) {
                for (Path existingFile : stream) {
                    Files.deleteIfExists(existingFile);
                }
            }

            String ext = StringUtils.getFilenameExtension(image.getOriginalFilename());
            String fileName = id + (ext != null ? "." + ext : "");
            Path filePath = uploadDir.resolve(fileName);

            Files.copy(image.getInputStream(), filePath);

            String profilePath = "/uploads/images/" + fileName;

            UserRecord userRecord = recordOpt.get();
            userRecord.setProfilePhotoUrl(profilePath);
            userRecord.setUser(userOpt.get());
            userRecordRepository.save(userRecord);

            return ResponseEntity.ok(
                    Map.of(
                            "status", "success",
                            "message", "Profile image uploaded successfully.",
                            "userId", id,
                            "imageUrl", profilePath
                    )
            );

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", "Error uploading image."));
        }
    }

    public ResponseEntity<?> enableUserTwoFactorKey(Boolean enable2fa, Authentication authentication) {
        String username = authentication.getName();
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (!optionalUser.isPresent()) {
            throw new BadRequestException("You don't have access to the endpoints", ErrorCode.FORBIDDEN_ACCESS);
        }
        User user = optionalUser.get();
        user.setTwoFactorEnabled(enable2fa);
        userRepository.save(user);
        return ResponseEntity.ok().body("Two-Factor Authentication updated successfully");
    }

    public ResponseEntity<?> removeUserProfileImage(Long id) {
        Optional<UserRecord> recordOpt = userRecordRepository.findByUserId(id);
        if (recordOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", "error", "message", "User record not found."));
        }

        UserRecord userRecord = recordOpt.get();
        String profilePath = userRecord.getProfilePhotoUrl();

        if (profilePath != null && !profilePath.isEmpty()) {
            try {
                Path uploadDir = Paths.get(fileStorageConfig.getUploadDir()).toAbsolutePath().normalize();
                Path filePath = uploadDir.resolve(profilePath.replaceFirst("^/", "")); 
                Files.deleteIfExists(filePath);
                userRecord.setProfilePhotoUrl(null);
                userRecordRepository.save(userRecord);

                return ResponseEntity.ok(Map.of("status", "success", "message", "Profile image removed."));
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("status", "error", "message", "Error removing image."));
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("status", "error", "message", "No profile image to remove."));
        }
    }
}
