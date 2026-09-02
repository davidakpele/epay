package com.epay.domain.developer.entity;

import com.epay.domain.developer.enums.WebhookDeliveryStatus;
import com.epay.domain.developer.enums.WebhookEventType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "webhook_delivery_logs", indexes = {
        @Index(name = "idx_wdl_webhook_id",  columnList = "webhook_id"),
        @Index(name = "idx_wdl_event_id",    columnList = "event_id"),
        @Index(name = "idx_wdl_status",      columnList = "status"),
        @Index(name = "idx_wdl_created_at",  columnList = "created_at")
})
public class WebhookDeliveryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "wdl_seq")
    @SequenceGenerator(name = "wdl_seq", sequenceName = "wdl_sequence", allocationSize = 1)
    private Long id;

    @Column(name = "webhook_id", nullable = false)
    private Long webhookId;

    @Column(name = "app_id", nullable = false)
    private Long appId;

    @Column(name = "event_id", nullable = false, length = 50)
    private String eventId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 40)
    private WebhookEventType eventType;

    @Column(name = "target_url", nullable = false, length = 500)
    private String targetUrl;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private WebhookDeliveryStatus status;

    @Column(name = "http_status_code")
    private Integer httpStatusCode;

    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "attempt_number", nullable = false)
    @Builder.Default
    private int attemptNumber = 1;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;
}
