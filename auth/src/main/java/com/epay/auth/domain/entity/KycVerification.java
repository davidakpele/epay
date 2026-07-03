package com.epay.auth.domain.entity;

import com.epay.domain.auth.enums.KycStatus;
import com.epay.domain.auth.enums.KycTier;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Tracks each KYC review cycle per user and tier.
 * A user may have multiple KycVerification records over time
 * (one per submission → review cycle).
 *
 * This is the compliance audit trail — regulators can ask for the full history.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "kyc_verifications", indexes = {
        @Index(name = "idx_kyc_ver_user_id", columnList = "user_id"),
        @Index(name = "idx_kyc_ver_status", columnList = "status"),
        @Index(name = "idx_kyc_ver_tier", columnList = "tier")
})
public class KycVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** The tier level this verification unlocks upon approval. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KycTier tier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KycStatus status;

    /** User ID of who submitted — the account holder or an admin acting on their behalf. */
    @Column(name = "submitted_by_user_id")
    private Long submittedByUserId;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    /** User ID of the compliance officer who reviewed. */
    @Column(name = "reviewed_by_user_id")
    private Long reviewedByUserId;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    /** Human-readable reason shown to user on rejection. */
    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    /** Internal compliance note — never exposed to the client. */
    @Column(name = "internal_note", length = 2000)
    private String internalNote;

    /** When this approval expires (some regulators require periodic renewal). */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /** Submission attempt number for this user + tier combo. */
    @Column(name = "attempt_count")
    private Integer attemptCount;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;
}
