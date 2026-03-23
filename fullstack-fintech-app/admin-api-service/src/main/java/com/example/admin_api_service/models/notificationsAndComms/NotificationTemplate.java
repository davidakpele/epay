package com.example.admin_api_service.models.notificationsAndComms;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.admin_api_service.enums.NotificationChannel;
import com.example.admin_api_service.enums.NotificationTemplateStatus;
import com.example.admin_api_service.enums.NotificationTemplateType;
import com.example.admin_api_service.enums.NotificationTriggerEvent;

@Entity
@Table(
    name = "notification_templates",
    uniqueConstraints = @UniqueConstraint(columnNames = {"template_key", "channel", "locale"})
)
public class NotificationTemplate {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id = UUID.randomUUID().toString();

    /**
     * Logical key used to look up this template at runtime.
     * e.g. "TRANSFER_SUCCESS", "KYC_APPROVED"
     * Unique per channel + locale combination (see table constraint above).
     */
    @Column(name = "template_key", length = 100, nullable = false)
    private String templateKey;

    /**
     * Human-readable code, unique across the whole table.
     * e.g. "TRANSFER_SUCCESS_EMAIL_EN"
     */
    @Column(name = "code", length = 200, nullable = false, unique = true)
    private String code;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    /**
     * Broad category of the template (TRANSACTIONAL, MARKETING, OTP, etc.).
     * Used for bulk queries via getTemplatesByType().
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 40, nullable = false)
    private NotificationTemplateType type;

    /**
     * Fine-grained event that triggered this notification.
     * Optional — may be null for manually-dispatched templates.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_event", length = 60)
    private NotificationTriggerEvent triggerEvent;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", length = 20, nullable = false)
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private NotificationTemplateStatus status = NotificationTemplateStatus.ACTIVE;

    /** ISO 639-1 language code e.g. "en", "fr", "yo" */
    @Column(name = "locale", length = 10, nullable = false)
    private String locale = "en";

    /** Email subject line — supports template variables e.g. "Your transfer of {{amount}} was successful" */
    @Column(name = "subject", length = 500)
    private String subject;

    /** Plain-text body — supports template variables */
    @Column(name = "body_text", columnDefinition = "text", nullable = false)
    private String bodyText;

    /** HTML body for email channel — supports template variables */
    @Column(name = "body_html", columnDefinition = "text")
    private String bodyHtml;

    /**
     * Available variables for this template stored as JSON array.
     * e.g. ["{{amount}}", "{{currency}}", "{{recipient_name}}", "{{reference}}"]
     */
    @Column(name = "available_variables", columnDefinition = "json")
    private String availableVariables;

    /** Sender name override — null uses platform default */
    @Column(name = "sender_name", length = 100)
    private String senderName;

    /** Sender address/number override — null uses platform default */
    @Column(name = "sender_address", length = 200)
    private String senderAddress;

    /** Whether this notification requires user consent to send */
    @Column(name = "requires_consent", nullable = false)
    private boolean requiresConsent = false;

    /**
     * Transactional = mandatory (receipts, OTPs, security alerts).
     * Non-transactional = marketing / promotional.
     * Transactional templates are protected from edit, deactivation, and deletion.
     */
    @Column(name = "is_transactional", nullable = false)
    private boolean isTransactional = true;

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

    public NotificationTemplate() {}

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTemplateKey() { return templateKey; }
    public void setTemplateKey(String templateKey) { this.templateKey = templateKey; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public NotificationTemplateType getType() { return type; }
    public void setType(NotificationTemplateType type) { this.type = type; }

    public NotificationTriggerEvent getTriggerEvent() { return triggerEvent; }
    public void setTriggerEvent(NotificationTriggerEvent triggerEvent) { this.triggerEvent = triggerEvent; }

    public NotificationChannel getChannel() { return channel; }
    public void setChannel(NotificationChannel channel) { this.channel = channel; }

    public NotificationTemplateStatus getStatus() { return status; }
    public void setStatus(NotificationTemplateStatus status) { this.status = status; }

    public String getLocale() { return locale; }
    public void setLocale(String locale) { this.locale = locale; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getBodyText() { return bodyText; }
    public void setBodyText(String bodyText) { this.bodyText = bodyText; }

    public String getBodyHtml() { return bodyHtml; }
    public void setBodyHtml(String bodyHtml) { this.bodyHtml = bodyHtml; }

    public String getAvailableVariables() { return availableVariables; }
    public void setAvailableVariables(String availableVariables) { this.availableVariables = availableVariables; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getSenderAddress() { return senderAddress; }
    public void setSenderAddress(String senderAddress) { this.senderAddress = senderAddress; }

    public boolean isRequiresConsent() { return requiresConsent; }
    public void setRequiresConsent(boolean requiresConsent) { this.requiresConsent = requiresConsent; }

    public boolean isTransactional() { return isTransactional; }
    public void setTransactional(boolean transactional) { isTransactional = transactional; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }

    public LocalDateTime getUpdatedOn() { return updatedOn; }
    public void setUpdatedOn(LocalDateTime updatedOn) { this.updatedOn = updatedOn; }
}