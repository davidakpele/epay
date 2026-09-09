package com.epay.domain.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "password_reset_tokens", indexes = {
        @Index(name = "idx_prt_user_id", columnList = "user_id"),
        @Index(name = "idx_prt_token_hash", columnList = "token_hash"),
        @Index(name = "idx_prt_expires_at", columnList = "expires_at")
})
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "prt_seq")
    @SequenceGenerator(name = "prt_seq", sequenceName = "password_reset_token_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "is_used", nullable = false)
    private boolean used;

    @Column(name = "requested_from_ip")
    private String requestedFromIp;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
