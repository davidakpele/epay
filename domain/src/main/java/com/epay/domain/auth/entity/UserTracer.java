package com.epay.domain.auth.entity;

import com.epay.domain.auth.enums.AttemptType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_tracer", indexes = {
        @Index(name = "idx_tracer_user_id", columnList = "user_id"),
        @Index(name = "idx_tracer_ip", columnList = "ip_address"),
        @Index(name = "idx_tracer_created_at", columnList = "created_at"),
        @Index(name = "idx_tracer_type", columnList = "attempt_type")
})
public class UserTracer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "attempt_type", nullable = false)
    private AttemptType attemptType;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "device_fingerprint")
    private String deviceFingerprint;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "location")
    private String location;

    @Column(name = "country_code")
    private String countryCode;

    @Column(name = "success", nullable = false)
    private boolean success;

    private String sessionId;

    @Column(name = "failure_reason")
    private String failureReason;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

    private boolean active;
}
