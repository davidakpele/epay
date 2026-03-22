package com.example.admin_api_service.models.userAndWalletManagement;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import com.example.admin_api_service.enums.AccountFlagSeverity;
import com.example.admin_api_service.enums.AccountFlagStatus;
import com.example.admin_api_service.enums.AccountFlagType;

@Entity
@Table(name = "account_flags")
public class AccountFlag {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id = UUID.randomUUID().toString();

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // Optional — null means flag applies at user level
    @Column(name = "wallet_id")
    private Long walletId;

    @Enumerated(EnumType.STRING)
    @Column(name = "flag_type", length = 50, nullable = false)
    private AccountFlagType flagType;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", length = 20, nullable = false)
    private AccountFlagSeverity severity = AccountFlagSeverity.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private AccountFlagStatus status = AccountFlagStatus.OPEN;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    // Source that raised the flag: SYSTEM (auto-detected) or MANUAL (admin raised)
    @Column(name = "source", length = 20, nullable = false)
    private String source;

    // Rule or system that triggered this flag (e.g. "AML_VELOCITY_CHECK", "SANCTION_SCREEN")
    @Column(name = "trigger_rule", length = 100)
    private String triggerRule;

    // Raw evidence or context stored as JSON (transaction IDs, scores, etc.)
    @Column(name = "evidence", columnDefinition = "json")
    private String evidence;

    @Column(name = "flagged_by", length = 36)
    private String flaggedBy;

    @Column(name = "flagged_at", nullable = false)
    private LocalDateTime flaggedAt = LocalDateTime.now();

    // Admin assigned to investigate this flag
    @Column(name = "assigned_to", length = 36)
    private String assignedTo;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "resolved_by", length = 36)
    private String resolvedBy;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolution_note", length = 500)
    private String resolutionNote;

    // Whether this flag triggered an account restriction or wallet freeze
    @Column(name = "triggered_restriction_id", length = 36)
    private String triggeredRestrictionId;

    @Column(name = "triggered_freeze_id", length = 36)
    private String triggeredFreezeId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn = LocalDateTime.now();

    @Column(name = "updated_on", nullable = false)
    private LocalDateTime updatedOn = LocalDateTime.now();

    // Navigation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "triggered_restriction_id", insertable = false, updatable = false)
    private AccountRestriction triggeredRestriction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "triggered_freeze_id", insertable = false, updatable = false)
    private WalletFreeze triggeredFreeze;

    @PreUpdate
    public void preUpdate() {
        this.updatedOn = LocalDateTime.now();
    }

    public AccountFlag() {
    }

    public AccountFlag(String id, Long userId, Long walletId, AccountFlagType flagType, AccountFlagSeverity severity, AccountFlagStatus status, String title, String description, String source, String triggerRule, String evidence, String flaggedBy, LocalDateTime flaggedAt, String assignedTo, LocalDateTime assignedAt, String resolvedBy, LocalDateTime resolvedAt, String resolutionNote, String triggeredRestrictionId, String triggeredFreezeId, String ipAddress, LocalDateTime createdOn, LocalDateTime updatedOn, AccountRestriction triggeredRestriction, WalletFreeze triggeredFreeze) {
        this.id = id;
        this.userId = userId;
        this.walletId = walletId;
        this.flagType = flagType;
        this.severity = severity;
        this.status = status;
        this.title = title;
        this.description = description;
        this.source = source;
        this.triggerRule = triggerRule;
        this.evidence = evidence;
        this.flaggedBy = flaggedBy;
        this.flaggedAt = flaggedAt;
        this.assignedTo = assignedTo;
        this.assignedAt = assignedAt;
        this.resolvedBy = resolvedBy;
        this.resolvedAt = resolvedAt;
        this.resolutionNote = resolutionNote;
        this.triggeredRestrictionId = triggeredRestrictionId;
        this.triggeredFreezeId = triggeredFreezeId;
        this.ipAddress = ipAddress;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
        this.triggeredRestriction = triggeredRestriction;
        this.triggeredFreeze = triggeredFreeze;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getWalletId() { return walletId; }
    public void setWalletId(Long walletId) { this.walletId = walletId; }

    public AccountFlagType getFlagType() { return flagType; }
    public void setFlagType(AccountFlagType flagType) { this.flagType = flagType; }

    public AccountFlagSeverity getSeverity() { return severity; }
    public void setSeverity(AccountFlagSeverity severity) { this.severity = severity; }

    public AccountFlagStatus getStatus() { return status; }
    public void setStatus(AccountFlagStatus status) { this.status = status; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getTriggerRule() { return triggerRule; }
    public void setTriggerRule(String triggerRule) { this.triggerRule = triggerRule; }

    public String getEvidence() { return evidence; }
    public void setEvidence(String evidence) { this.evidence = evidence; }

    public String getFlaggedBy() { return flaggedBy; }
    public void setFlaggedBy(String flaggedBy) { this.flaggedBy = flaggedBy; }

    public LocalDateTime getFlaggedAt() { return flaggedAt; }
    public void setFlaggedAt(LocalDateTime flaggedAt) { this.flaggedAt = flaggedAt; }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }

    public String getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public String getResolutionNote() { return resolutionNote; }
    public void setResolutionNote(String resolutionNote) { this.resolutionNote = resolutionNote; }

    public String getTriggeredRestrictionId() { return triggeredRestrictionId; }
    public void setTriggeredRestrictionId(String triggeredRestrictionId) { this.triggeredRestrictionId = triggeredRestrictionId; }

    public String getTriggeredFreezeId() { return triggeredFreezeId; }
    public void setTriggeredFreezeId(String triggeredFreezeId) { this.triggeredFreezeId = triggeredFreezeId; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }

    public LocalDateTime getUpdatedOn() { return updatedOn; }
    public void setUpdatedOn(LocalDateTime updatedOn) { this.updatedOn = updatedOn; }

    public AccountRestriction getTriggeredRestriction() { return triggeredRestriction; }
    public void setTriggeredRestriction(AccountRestriction triggeredRestriction) { this.triggeredRestriction = triggeredRestriction; }

    public WalletFreeze getTriggeredFreeze() { return triggeredFreeze; }
    public void setTriggeredFreeze(WalletFreeze triggeredFreeze) { this.triggeredFreeze = triggeredFreeze; }
}
