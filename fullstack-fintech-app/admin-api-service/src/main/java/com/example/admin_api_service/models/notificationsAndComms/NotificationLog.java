package com.example.admin_api_service.models.notificationsAndComms;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.admin_api_service.enums.NotificationChannel;
import com.example.admin_api_service.enums.NotificationLogStatus;
import com.example.admin_api_service.enums.NotificationPriority;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "notification_logs")
public class NotificationLog {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id = UUID.randomUUID().toString();

    // Linked template used to render this notification — null if ad-hoc
    @Column(name = "template_id", length = 36)
    private String templateId;

    // Template key snapshot at send time — preserved even if template is later changed
    @Column(name = "template_key", length = 100)
    private String templateKey;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "wallet_id")
    private Long walletId;

    // The entity that triggered this notification e.g. transactionId, kycId
    @Column(name = "reference_id", length = 36)
    private String referenceId;

    // Type of entity in referenceId e.g. "TRANSACTION", "KYC", "WALLET_FREEZE"
    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", length = 20, nullable = false)
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private NotificationLogStatus status = NotificationLogStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 10, nullable = false)
    private NotificationPriority priority = NotificationPriority.NORMAL;

    // Recipient — email address, phone number, or device token
    @Column(name = "recipient", length = 300, nullable = false)
    private String recipient;

    @Column(name = "subject", length = 300)
    private String subject;

    // Rendered final body (after variable substitution)
    @Column(name = "body", columnDefinition = "text", nullable = false)
    private String body;

    // Variable values used to render the template stored as JSON
    @Column(name = "variables_used", columnDefinition = "json")
    private String variablesUsed;

    // Reference returned by the sending provider e.g. SendGrid message ID, Termii message ID
    @Column(name = "provider_name", length = 100)
    private String providerName;

    @Column(name = "provider_reference", length = 200)
    private String providerReference;

    // Full provider response stored as JSON
    @Column(name = "provider_response", columnDefinition = "json")
    private String providerResponse;

    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;

    @Column(name = "max_retries", nullable = false)
    private int maxRetries = 3;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    // Timestamp when the provider confirmed delivery
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    // Timestamp when the recipient opened the notification (email open tracking)
    @Column(name = "opened_at")
    private LocalDateTime openedAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn = LocalDateTime.now();

    @Column(name = "updated_on", nullable = false)
    private LocalDateTime updatedOn = LocalDateTime.now();

    // Navigation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", insertable = false, updatable = false)
    private NotificationTemplate template;

    @PreUpdate
    public void preUpdate() { this.updatedOn = LocalDateTime.now(); }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }

    public String getTemplateKey() { return templateKey; }
    public void setTemplateKey(String templateKey) { this.templateKey = templateKey; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getWalletId() { return walletId; }
    public void setWalletId(Long walletId) { this.walletId = walletId; }

    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }

    public String getReferenceType() { return referenceType; }
    public void setReferenceType(String referenceType) { this.referenceType = referenceType; }

    public NotificationChannel getChannel() { return channel; }
    public void setChannel(NotificationChannel channel) { this.channel = channel; }

    public NotificationLogStatus getStatus() { return status; }
    public void setStatus(NotificationLogStatus status) { this.status = status; }

    public NotificationPriority getPriority() { return priority; }
    public void setPriority(NotificationPriority priority) { this.priority = priority; }

    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getVariablesUsed() { return variablesUsed; }
    public void setVariablesUsed(String variablesUsed) { this.variablesUsed = variablesUsed; }

    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }

    public String getProviderReference() { return providerReference; }
    public void setProviderReference(String providerReference) { this.providerReference = providerReference; }

    public String getProviderResponse() { return providerResponse; }
    public void setProviderResponse(String providerResponse) { this.providerResponse = providerResponse; }

    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }

    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }

    public LocalDateTime getOpenedAt() { return openedAt; }
    public void setOpenedAt(LocalDateTime openedAt) { this.openedAt = openedAt; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }

    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }

    public LocalDateTime getUpdatedOn() { return updatedOn; }
    public void setUpdatedOn(LocalDateTime updatedOn) { this.updatedOn = updatedOn; }

    public NotificationTemplate getTemplate() { return template; }
    public void setTemplate(NotificationTemplate template) { this.template = template; }
}