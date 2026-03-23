package com.example.admin_api_service.models.notificationsAndComms;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.admin_api_service.enums.NotificationChannel;
import com.example.admin_api_service.enums.NotificationLogStatus;
import com.example.admin_api_service.enums.NotificationPriority;
import com.example.admin_api_service.enums.NotificationTriggerEvent;

@Entity
@Table(name = "notification_logs")
public class NotificationLog {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id = UUID.randomUUID().toString();

    @Column(name = "template_id", length = 36)
    private String templateId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "wallet_id")
    private Long walletId;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_event", length = 60)
    private NotificationTriggerEvent triggerEvent;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", length = 20, nullable = false)
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private NotificationLogStatus status = NotificationLogStatus.PENDING;

    /**
     * Dispatch priority — controls queue ordering in the sending worker.
     * Defaults to NORMAL.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 20, nullable = false)
    private NotificationPriority priority = NotificationPriority.NORMAL;

    /** Recipient address: email address, phone number, or device token */
    @Column(name = "recipient_address", length = 300, nullable = false)
    private String recipientAddress;

    @Column(name = "subject", length = 500)
    private String subject;

    /** Final rendered body after variable substitution */
    @Column(name = "body", columnDefinition = "text", nullable = false)
    private String body;

    /** Context data used to render the template, stored as JSON */
    @Column(name = "template_variables", columnDefinition = "json")
    private String templateVariables;

    /** ID of the entity that triggered this notification (transactionId, kycId, etc.) */
    @Column(name = "source_entity_id", length = 36)
    private String sourceEntityId;

    /** Type of the source entity e.g. "TRANSACTION", "KYC", "CHARGEBACK" */
    @Column(name = "source_entity_type", length = 50)
    private String sourceEntityType;

    /** Reference returned by the notification provider (e.g. SendGrid message ID, Twilio SID) */
    @Column(name = "provider_reference", length = 200)
    private String providerReference;

    /** Name of the provider used e.g. "SendGrid", "Termii", "Firebase" */
    @Column(name = "provider_name", length = 100)
    private String providerName;

    /** Full provider response stored as JSON */
    @Column(name = "provider_response", columnDefinition = "json")
    private String providerResponse;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;

    @Column(name = "max_retries", nullable = false)
    private int maxRetries = 3;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    /** Populated when the recipient opens the message (email open tracking, in-app read) */
    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    /**
     * When set, this notification will not be dispatched until this timestamp is reached.
     * Null means dispatch immediately.
     */
    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    /** Whether this was triggered by the system or manually sent by an admin */
    @Column(name = "is_manual", nullable = false)
    private boolean isManual = false;

    @Column(name = "initiated_by", length = 36)
    private String initiatedBy;

    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn = LocalDateTime.now();

    @Column(name = "updated_on", nullable = false)
    private LocalDateTime updatedOn = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", insertable = false, updatable = false)
    private NotificationTemplate template;

    @PreUpdate
    public void preUpdate() { this.updatedOn = LocalDateTime.now(); }

    public NotificationLog() {}

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getWalletId() { return walletId; }
    public void setWalletId(Long walletId) { this.walletId = walletId; }

    public NotificationTriggerEvent getTriggerEvent() { return triggerEvent; }
    public void setTriggerEvent(NotificationTriggerEvent triggerEvent) { this.triggerEvent = triggerEvent; }

    public NotificationChannel getChannel() { return channel; }
    public void setChannel(NotificationChannel channel) { this.channel = channel; }

    public NotificationLogStatus getStatus() { return status; }
    public void setStatus(NotificationLogStatus status) { this.status = status; }

    public NotificationPriority getPriority() { return priority; }
    public void setPriority(NotificationPriority priority) { this.priority = priority; }

    public String getRecipientAddress() { return recipientAddress; }
    public void setRecipientAddress(String recipientAddress) { this.recipientAddress = recipientAddress; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getTemplateVariables() { return templateVariables; }
    public void setTemplateVariables(String templateVariables) { this.templateVariables = templateVariables; }

    public String getSourceEntityId() { return sourceEntityId; }
    public void setSourceEntityId(String sourceEntityId) { this.sourceEntityId = sourceEntityId; }

    public String getSourceEntityType() { return sourceEntityType; }
    public void setSourceEntityType(String sourceEntityType) { this.sourceEntityType = sourceEntityType; }

    public String getProviderReference() { return providerReference; }
    public void setProviderReference(String providerReference) { this.providerReference = providerReference; }

    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }

    public String getProviderResponse() { return providerResponse; }
    public void setProviderResponse(String providerResponse) { this.providerResponse = providerResponse; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }

    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }

    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }

    public LocalDateTime getNextRetryAt() { return nextRetryAt; }
    public void setNextRetryAt(LocalDateTime nextRetryAt) { this.nextRetryAt = nextRetryAt; }

    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }

    public boolean isManual() { return isManual; }
    public void setManual(boolean manual) { isManual = manual; }

    public String getInitiatedBy() { return initiatedBy; }
    public void setInitiatedBy(String initiatedBy) { this.initiatedBy = initiatedBy; }

    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }

    public LocalDateTime getUpdatedOn() { return updatedOn; }
    public void setUpdatedOn(LocalDateTime updatedOn) { this.updatedOn = updatedOn; }

    public NotificationTemplate getTemplate() { return template; }
    public void setTemplate(NotificationTemplate template) { this.template = template; }
}