package com.epay.domain.developer.entity;

import com.epay.domain.developer.enums.ApiMode;
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
@Table(name = "app_webhooks", indexes = {
        @Index(name = "idx_webhook_app_mode", columnList = "app_id, mode")
})
public class AppWebhook {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "app_webhook_seq")
    @SequenceGenerator(name = "app_webhook_seq", sequenceName = "app_webhook_sequence", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "app_id", nullable = false)
    private DeveloperApp app;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ApiMode mode;

    @Column(name = "url", nullable = false, length = 500)
    private String url;

    @Column(name = "signing_secret", nullable = false, length = 100)
    private String signingSecret;

    @Column(name = "subscribed_events", length = 1000)
    private String subscribedEvents;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "success_count", nullable = false)
    @Builder.Default
    private long successCount = 0L;

    @Column(name = "failure_count", nullable = false)
    @Builder.Default
    private long failureCount = 0L;

    @Column(name = "last_delivery_at")
    private LocalDateTime lastDeliveryAt;

    @Column(name = "last_delivery_status", length = 20)
    private String lastDeliveryStatus;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
