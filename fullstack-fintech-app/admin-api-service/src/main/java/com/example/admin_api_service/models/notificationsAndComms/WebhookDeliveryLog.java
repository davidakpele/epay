package com.example.admin_api_service.models.notificationsAndComms;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.admin_api_service.enums.WebhookDeliveryStatus;

@Entity
@Table(name = "webhook_delivery_logs")
public class WebhookDeliveryLog {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id = UUID.randomUUID().toString();

    @Column(name = "webhook_endpoint_id", length = 36, nullable = false)
    private String webhookEndpointId;

    // The event that triggered this delivery e.g. "TRANSFER_SUCCESS"
    @Column(name = "event_type", length = 100, nullable = false)
    private String eventType;

    // ID of the entity that triggered the event
    @Column(name = "event_reference_id", length = 36)
    private String eventReferenceId;

    // Type of entity e.g. "TRANSACTION", "KYC", "WALLET_FREEZE"
    @Column(name = "event_reference_type", length = 50)
    private String eventReferenceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private WebhookDeliveryStatus status = WebhookDeliveryStatus.PENDING;

    // Idempotency key — same event will not be delivered twice to the same endpoint
    @Column(name = "idempotency_key", length = 100, nullable = false, unique = true)
    private String idempotencyKey;

    // Full JSON payload sent to the endpoint
    @Column(name = "request_payload", columnDefinition = "json", nullable = false)
    private String requestPayload;

    // Request headers sent (sensitive values masked) stored as JSON
    @Column(name = "request_headers", columnDefinition = "json")
    private String requestHeaders;

    // HTTP status code received from the endpoint
    @Column(name = "response_status_code")
    private Integer responseStatusCode;

    // Response body from the endpoint (truncated if > 2KB)
    @Column(name = "response_body", columnDefinition = "text")
    private String responseBody;

    // Response headers received stored as JSON
    @Column(name = "response_headers", columnDefinition = "json")
    private String responseHeaders;

    // Delivery duration in milliseconds
    @Column(name = "duration_ms")
    private Long durationMs;

    // Which attempt this is (1 = first attempt, 2 = first retry, etc.)
    @Column(name = "attempt_number", nullable = false)
    private int attemptNumber = 1;

    // Whether this delivery was manually triggered by an admin (replay)
    @Column(name = "is_manual_replay", nullable = false)
    private boolean isManualReplay = false;

    @Column(name = "replayed_by", length = 36)
    private String replayedBy;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    // When the next retry is scheduled — null if no retry pending
    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn = LocalDateTime.now();

    @Column(name = "updated_on", nullable = false)
    private LocalDateTime updatedOn = LocalDateTime.now();

    // Navigation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "webhook_endpoint_id", insertable = false, updatable = false)
    private WebhookEndpoint webhookEndpoint;

    @PreUpdate
    public void preUpdate() { this.updatedOn = LocalDateTime.now(); }

    public WebhookDeliveryLog() {
    }

    public WebhookDeliveryLog(String id, String webhookEndpointId, String eventType, String eventReferenceId, String eventReferenceType, WebhookDeliveryStatus status, String idempotencyKey, String requestPayload, String requestHeaders, Integer responseStatusCode, String responseBody, String responseHeaders, Long durationMs, int attemptNumber, boolean isManualReplay, String replayedBy, String failureReason, LocalDateTime nextRetryAt, LocalDateTime deliveredAt, LocalDateTime createdOn, LocalDateTime updatedOn, WebhookEndpoint webhookEndpoint) {
        this.id = id;
        this.webhookEndpointId = webhookEndpointId;
        this.eventType = eventType;
        this.eventReferenceId = eventReferenceId;
        this.eventReferenceType = eventReferenceType;
        this.status = status;
        this.idempotencyKey = idempotencyKey;
        this.requestPayload = requestPayload;
        this.requestHeaders = requestHeaders;
        this.responseStatusCode = responseStatusCode;
        this.responseBody = responseBody;
        this.responseHeaders = responseHeaders;
        this.durationMs = durationMs;
        this.attemptNumber = attemptNumber;
        this.isManualReplay = isManualReplay;
        this.replayedBy = replayedBy;
        this.failureReason = failureReason;
        this.nextRetryAt = nextRetryAt;
        this.deliveredAt = deliveredAt;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
        this.webhookEndpoint = webhookEndpoint;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getWebhookEndpointId() { return webhookEndpointId; }
    public void setWebhookEndpointId(String webhookEndpointId) { this.webhookEndpointId = webhookEndpointId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getEventReferenceId() { return eventReferenceId; }
    public void setEventReferenceId(String eventReferenceId) { this.eventReferenceId = eventReferenceId; }

    public String getEventReferenceType() { return eventReferenceType; }
    public void setEventReferenceType(String eventReferenceType) { this.eventReferenceType = eventReferenceType; }

    public WebhookDeliveryStatus getStatus() { return status; }
    public void setStatus(WebhookDeliveryStatus status) { this.status = status; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public String getRequestPayload() { return requestPayload; }
    public void setRequestPayload(String requestPayload) { this.requestPayload = requestPayload; }

    public String getRequestHeaders() { return requestHeaders; }
    public void setRequestHeaders(String requestHeaders) { this.requestHeaders = requestHeaders; }

    public Integer getResponseStatusCode() { return responseStatusCode; }
    public void setResponseStatusCode(Integer responseStatusCode) { this.responseStatusCode = responseStatusCode; }

    public String getResponseBody() { return responseBody; }
    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }

    public String getResponseHeaders() { return responseHeaders; }
    public void setResponseHeaders(String responseHeaders) { this.responseHeaders = responseHeaders; }

    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }

    public int getAttemptNumber() { return attemptNumber; }
    public void setAttemptNumber(int attemptNumber) { this.attemptNumber = attemptNumber; }

    public boolean isManualReplay() { return isManualReplay; }
    public void setManualReplay(boolean manualReplay) { isManualReplay = manualReplay; }

    public String getReplayedBy() { return replayedBy; }
    public void setReplayedBy(String replayedBy) { this.replayedBy = replayedBy; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public LocalDateTime getNextRetryAt() { return nextRetryAt; }
    public void setNextRetryAt(LocalDateTime nextRetryAt) { this.nextRetryAt = nextRetryAt; }

    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }

    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }

    public LocalDateTime getUpdatedOn() { return updatedOn; }
    public void setUpdatedOn(LocalDateTime updatedOn) { this.updatedOn = updatedOn; }

    public WebhookEndpoint getWebhookEndpoint() { return webhookEndpoint; }
    public void setWebhookEndpoint(WebhookEndpoint webhookEndpoint) { this.webhookEndpoint = webhookEndpoint; }
}