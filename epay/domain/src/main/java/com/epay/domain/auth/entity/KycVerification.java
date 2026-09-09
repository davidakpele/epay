package com.epay.domain.auth.entity;

import com.epay.domain.auth.enums.KycStatus;
import com.epay.domain.auth.enums.KycTier;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "kyc_ver_seq")
    @SequenceGenerator(name = "kyc_ver_seq", sequenceName = "kyc_verification_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KycTier tier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KycStatus status;

    @Column(name = "submitted_by_user_id")
    private Long submittedByUserId;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "reviewed_by_user_id")
    private Long reviewedByUserId;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    @Column(name = "internal_note", length = 2000)
    private String internalNote;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

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
