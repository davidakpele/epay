package com.example.admin_api_service.models.notificationsAndComms;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import com.example.admin_api_service.enums.WebhookEndpointStatus;

@Entity
@Table(name = "webhook_endpoints")
public class WebhookEndpoint {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id = UUID.randomUUID().toString();

    // Owner — can be a partner, merchant, or internal service
    @Column(name = "owner_id", length = 36, nullable = false)
    private String ownerId;

    // Type of owner: PARTNER, MERCHANT, INTERNAL
    @Column(name = "owner_type", length = 20, nullable = false)
    private String ownerType;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "url", length = 500, nullable = false)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private WebhookEndpointStatus status = WebhookEndpointStatus.ACTIVE;

    // Hashed HMAC secret used to sign payloads — never store plaintext
    @Column(name = "secret_hash", length = 500, nullable = false)
    private String secretHash;

    // Events this endpoint is subscribed to stored as JSON array
    // e.g. ["TRANSFER_SUCCESS", "TRANSFER_FAILED", "KYC_APPROVED"]
    @Column(name = "subscribed_events", columnDefinition = "json", nullable = false)
    private String subscribedEvents;

    // HTTP headers to include in every delivery stored as JSON object
    @Column(name = "custom_headers", columnDefinition = "json")
    private String customHeaders;

    // HTTP method — defaults to POST
    @Column(name = "http_method", length = 10, nullable = false)
    private String httpMethod = "POST";

    // Timeout in seconds for delivery attempts
    @Column(name = "timeout_seconds", nullable = false)
    private int timeoutSeconds = 30;

    // Maximum number of retry attempts on failure
    @Column(name = "max_retries", nullable = false)
    private int maxRetries = 3;

    // Whether TLS certificate verification is enforced
    @Column(name = "ssl_verify", nullable = false)
    private boolean sslVerify = true;

    // Whether this endpoint is active in the current environment
    @Column(name = "environment", length = 20, nullable = false)
    private String environment = "PRODUCTION";

    // Running counters — updated incrementally
    @Column(name = "total_deliveries", nullable = false)
    private long totalDeliveries = 0;

    @Column(name = "successful_deliveries", nullable = false)
    private long successfulDeliveries = 0;

    @Column(name = "failed_deliveries", nullable = false)
    private long failedDeliveries = 0;

    // Timestamp of the most recent successful delivery
    @Column(name = "last_success_at")
    private LocalDateTime lastSuccessAt;

    // Timestamp of the most recent failed delivery
    @Column(name = "last_failure_at")
    private LocalDateTime lastFailureAt;

    // Auto-disable after consecutive failure threshold
    @Column(name = "consecutive_failures", nullable = false)
    private int consecutiveFailures = 0;

    // Threshold after which endpoint is auto-disabled
    @Column(name = "disable_after_failures", nullable = false)
    private int disableAfterFailures = 10;

    @Column(name = "disabled_reason", length = 500)
    private String disabledReason;

    @Column(name = "created_by", length = 36)
    private String createdBy;

    @Column(name = "updated_by", length = 36)
    private String updatedBy;

    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn = LocalDateTime.now();

    @Column(name = "updated_on", nullable = false)
    private LocalDateTime updatedOn = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() { this.updatedOn = LocalDateTime.now(); }

    public WebhookEndpoint() {
    }

    public WebhookEndpoint(String id, String ownerId, String ownerType, String name, String description, String url, WebhookEndpointStatus status, String secretHash, String subscribedEvents, String customHeaders, String httpMethod, int timeoutSeconds, int maxRetries, boolean sslVerify, String environment, long totalDeliveries, long successfulDeliveries, long failedDeliveries, LocalDateTime lastSuccessAt, LocalDateTime lastFailureAt, int consecutiveFailures, int disableAfterFailures, String disabledReason, String createdBy, String updatedBy, LocalDateTime createdOn, LocalDateTime updatedOn) {
        this.id = id;
        this.ownerId = ownerId;
        this.ownerType = ownerType;
        this.name = name;
        this.description = description;
        this.url = url;
        this.status = status;
        this.secretHash = secretHash;
        this.subscribedEvents = subscribedEvents;
        this.customHeaders = customHeaders;
        this.httpMethod = httpMethod;
        this.timeoutSeconds = timeoutSeconds;
        this.maxRetries = maxRetries;
        this.sslVerify = sslVerify;
        this.environment = environment;
        this.totalDeliveries = totalDeliveries;
        this.successfulDeliveries = successfulDeliveries;
        this.failedDeliveries = failedDeliveries;
        this.lastSuccessAt = lastSuccessAt;
        this.lastFailureAt = lastFailureAt;
        this.consecutiveFailures = consecutiveFailures;
        this.disableAfterFailures = disableAfterFailures;
        this.disabledReason = disabledReason;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public String getOwnerType() { return ownerType; }
    public void setOwnerType(String ownerType) { this.ownerType = ownerType; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public WebhookEndpointStatus getStatus() { return status; }
    public void setStatus(WebhookEndpointStatus status) { this.status = status; }

    public String getSecretHash() { return secretHash; }
    public void setSecretHash(String secretHash) { this.secretHash = secretHash; }

    public String getSubscribedEvents() { return subscribedEvents; }
    public void setSubscribedEvents(String subscribedEvents) { this.subscribedEvents = subscribedEvents; }

    public String getCustomHeaders() { return customHeaders; }
    public void setCustomHeaders(String customHeaders) { this.customHeaders = customHeaders; }

    public String getHttpMethod() { return httpMethod; }
    public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }

    public int getTimeoutSeconds() { return timeoutSeconds; }
    public void setTimeoutSeconds(int timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }

    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }

    public boolean isSslVerify() { return sslVerify; }
    public void setSslVerify(boolean sslVerify) { this.sslVerify = sslVerify; }

    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }

    public long getTotalDeliveries() { return totalDeliveries; }
    public void setTotalDeliveries(long totalDeliveries) { this.totalDeliveries = totalDeliveries; }

    public long getSuccessfulDeliveries() { return successfulDeliveries; }
    public void setSuccessfulDeliveries(long successfulDeliveries) { this.successfulDeliveries = successfulDeliveries; }

    public long getFailedDeliveries() { return failedDeliveries; }
    public void setFailedDeliveries(long failedDeliveries) { this.failedDeliveries = failedDeliveries; }

    public LocalDateTime getLastSuccessAt() { return lastSuccessAt; }
    public void setLastSuccessAt(LocalDateTime lastSuccessAt) { this.lastSuccessAt = lastSuccessAt; }

    public LocalDateTime getLastFailureAt() { return lastFailureAt; }
    public void setLastFailureAt(LocalDateTime lastFailureAt) { this.lastFailureAt = lastFailureAt; }

    public int getConsecutiveFailures() { return consecutiveFailures; }
    public void setConsecutiveFailures(int consecutiveFailures) { this.consecutiveFailures = consecutiveFailures; }

    public int getDisableAfterFailures() { return disableAfterFailures; }
    public void setDisableAfterFailures(int disableAfterFailures) { this.disableAfterFailures = disableAfterFailures; }

    public String getDisabledReason() { return disabledReason; }
    public void setDisabledReason(String disabledReason) { this.disabledReason = disabledReason; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }

    public LocalDateTime getUpdatedOn() { return updatedOn; }
    public void setUpdatedOn(LocalDateTime updatedOn) { this.updatedOn = updatedOn; }
}