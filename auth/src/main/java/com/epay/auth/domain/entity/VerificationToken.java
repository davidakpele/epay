package com.epay.auth.domain.entity;

import com.epay.domain.auth.enums.ContactMethod;
import com.epay.domain.auth.enums.TokenPurpose;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * OTP / verification tokens for email and phone confirmation.
 * Tokens are hashed before storage — never store plaintext OTPs.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "verification_tokens", indexes = {
        @Index(name = "idx_vt_user_id", columnList = "user_id"),
        @Index(name = "idx_vt_token_hash", columnList = "token_hash"),
        @Index(name = "idx_vt_expires_at", columnList = "expires_at")
})
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * SHA-256 hash of the OTP — never store the raw token.
     * Compare by hashing the incoming token and matching against this.
     */
    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    /** Channel through which the token was delivered. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContactMethod channel;

    /** What this token is verifying. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TokenPurpose purpose;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "is_used", nullable = false)
    private boolean used;

    /** Number of failed verification attempts against this token. */
    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    /** IP that requested this token. */
    @Column(name = "requested_from_ip")
    private String requestedFromIp;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
