package com.epay.domain.developer.entity;

import com.epay.domain.developer.enums.ApiKeyStatus;
import com.epay.domain.developer.enums.ApiMode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "api_keys", indexes = {
        @Index(name = "idx_api_key_public",  columnList = "public_key",  unique = true),
        @Index(name = "idx_api_key_app_mode",columnList = "app_id, mode")
})
public class ApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "api_key_seq")
    @SequenceGenerator(name = "api_key_seq", sequenceName = "api_key_sequence", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "app_id", nullable = false)
    private DeveloperApp app;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ApiMode mode;

    @Column(name = "public_key", nullable = false, unique = true, length = 80)
    private String publicKey;


    @Column(name = "secret_key_hash", nullable = false, length = 100)
    private String secretKeyHash;

    @Column(name = "secret_key_prefix", nullable = false, length = 20)
    private String secretKeyPrefix;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    @Builder.Default
    private ApiKeyStatus status = ApiKeyStatus.ACTIVE;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "request_count", nullable = false)
    @Builder.Default
    private long requestCount = 0L;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "revoked_by")
    private Long revokedBy;

    @Column(name = "revoke_reason", length = 300)
    private String revokeReason;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;
}
