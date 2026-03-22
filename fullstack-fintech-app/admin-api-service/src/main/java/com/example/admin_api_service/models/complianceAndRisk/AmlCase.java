package com.example.admin_api_service.models.complianceAndRisk;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.admin_api_service.enums.AmlCasePriority;
import com.example.admin_api_service.enums.AmlCaseStatus;
import com.example.admin_api_service.enums.AmlCaseType;
import com.example.admin_api_service.models.userAndWalletManagement.WalletFreeze;

@Entity
@Table(name = "aml_cases")
public class AmlCase {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id = UUID.randomUUID().toString();

    // Human-readable case reference e.g. AML-2024-00123
    @Column(name = "case_reference", length = 50, nullable = false, unique = true)
    private String caseReference;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "wallet_id")
    private Long walletId;

    @Enumerated(EnumType.STRING)
    @Column(name = "case_type", length = 50, nullable = false)
    private AmlCaseType caseType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private AmlCaseStatus status = AmlCaseStatus.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 20, nullable = false)
    private AmlCasePriority priority = AmlCasePriority.MEDIUM;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "description", length = 2000)
    private String description;

    // Summary of suspicious activity stored as JSON
    @Column(name = "activity_summary", columnDefinition = "json")
    private String activitySummary;

    // IDs of transactions flagged in this case stored as JSON array
    @Column(name = "flagged_transaction_ids", columnDefinition = "json")
    private String flaggedTransactionIds;

    // Links to AmlAlerts that triggered this case
    @Column(name = "source_alert_ids", columnDefinition = "json")
    private String sourceAlertIds;

    // Whether a Suspicious Activity Report was filed
    @Column(name = "sar_filed", nullable = false)
    private boolean sarFiled = false;

    @Column(name = "sar_reference", length = 100)
    private String sarReference;

    @Column(name = "sar_filed_at")
    private LocalDateTime sarFiledAt;

    @Column(name = "sar_filed_by", length = 36)
    private String sarFiledBy;

    @Column(name = "assigned_to", length = 36)
    private String assignedTo;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "escalated_to", length = 36)
    private String escalatedTo;

    @Column(name = "escalated_at")
    private LocalDateTime escalatedAt;

    @Column(name = "escalation_reason", length = 500)
    private String escalationReason;

    @Column(name = "opened_by", length = 36, nullable = false)
    private String openedBy;

    @Column(name = "opened_at", nullable = false)
    private LocalDateTime openedAt = LocalDateTime.now();

    @Column(name = "closed_by", length = 36)
    private String closedBy;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "closure_note", length = 1000)
    private String closureNote;

    // Linked wallet freeze triggered by this case
    @Column(name = "triggered_freeze_id", length = 36)
    private String triggeredFreezeId;

    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn = LocalDateTime.now();

    @Column(name = "updated_on", nullable = false)
    private LocalDateTime updatedOn = LocalDateTime.now();

    // Navigation
    @OneToMany(mappedBy = "amlCase", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AmlAlert> alerts = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "triggered_freeze_id", insertable = false, updatable = false)
    private WalletFreeze triggeredFreeze;

    @PreUpdate
    public void preUpdate() {
        this.updatedOn = LocalDateTime.now();
    }

    public AmlCase() {
    }

    public AmlCase(String id, String caseReference, Long userId, Long walletId, AmlCaseType caseType, AmlCaseStatus status, AmlCasePriority priority, String title, String description, String activitySummary, String flaggedTransactionIds, String sourceAlertIds, boolean sarFiled, String sarReference, LocalDateTime sarFiledAt, String sarFiledBy, String assignedTo, LocalDateTime assignedAt, String escalatedTo, LocalDateTime escalatedAt, String escalationReason, String openedBy, LocalDateTime openedAt, String closedBy, LocalDateTime closedAt, String closureNote, String triggeredFreezeId, LocalDateTime createdOn, LocalDateTime updatedOn, List<AmlAlert> alerts, WalletFreeze triggeredFreeze) {
        this.id = id;
        this.caseReference = caseReference;
        this.userId = userId;
        this.walletId = walletId;
        this.caseType = caseType;
        this.status = status;
        this.priority = priority;
        this.title = title;
        this.description = description;
        this.activitySummary = activitySummary;
        this.flaggedTransactionIds = flaggedTransactionIds;
        this.sourceAlertIds = sourceAlertIds;
        this.sarFiled = sarFiled;
        this.sarReference = sarReference;
        this.sarFiledAt = sarFiledAt;
        this.sarFiledBy = sarFiledBy;
        this.assignedTo = assignedTo;
        this.assignedAt = assignedAt;
        this.escalatedTo = escalatedTo;
        this.escalatedAt = escalatedAt;
        this.escalationReason = escalationReason;
        this.openedBy = openedBy;
        this.openedAt = openedAt;
        this.closedBy = closedBy;
        this.closedAt = closedAt;
        this.closureNote = closureNote;
        this.triggeredFreezeId = triggeredFreezeId;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
        this.alerts = alerts;
        this.triggeredFreeze = triggeredFreeze;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCaseReference() { return caseReference; }
    public void setCaseReference(String caseReference) { this.caseReference = caseReference; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getWalletId() { return walletId; }
    public void setWalletId(Long walletId) { this.walletId = walletId; }

    public AmlCaseType getCaseType() { return caseType; }
    public void setCaseType(AmlCaseType caseType) { this.caseType = caseType; }

    public AmlCaseStatus getStatus() { return status; }
    public void setStatus(AmlCaseStatus status) { this.status = status; }

    public AmlCasePriority getPriority() { return priority; }
    public void setPriority(AmlCasePriority priority) { this.priority = priority; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getActivitySummary() { return activitySummary; }
    public void setActivitySummary(String activitySummary) { this.activitySummary = activitySummary; }

    public String getFlaggedTransactionIds() { return flaggedTransactionIds; }
    public void setFlaggedTransactionIds(String flaggedTransactionIds) { this.flaggedTransactionIds = flaggedTransactionIds; }

    public String getSourceAlertIds() { return sourceAlertIds; }
    public void setSourceAlertIds(String sourceAlertIds) { this.sourceAlertIds = sourceAlertIds; }

    public boolean isSarFiled() { return sarFiled; }
    public void setSarFiled(boolean sarFiled) { this.sarFiled = sarFiled; }

    public String getSarReference() { return sarReference; }
    public void setSarReference(String sarReference) { this.sarReference = sarReference; }

    public LocalDateTime getSarFiledAt() { return sarFiledAt; }
    public void setSarFiledAt(LocalDateTime sarFiledAt) { this.sarFiledAt = sarFiledAt; }

    public String getSarFiledBy() { return sarFiledBy; }
    public void setSarFiledBy(String sarFiledBy) { this.sarFiledBy = sarFiledBy; }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }

    public String getEscalatedTo() { return escalatedTo; }
    public void setEscalatedTo(String escalatedTo) { this.escalatedTo = escalatedTo; }

    public LocalDateTime getEscalatedAt() { return escalatedAt; }
    public void setEscalatedAt(LocalDateTime escalatedAt) { this.escalatedAt = escalatedAt; }

    public String getEscalationReason() { return escalationReason; }
    public void setEscalationReason(String escalationReason) { this.escalationReason = escalationReason; }

    public String getOpenedBy() { return openedBy; }
    public void setOpenedBy(String openedBy) { this.openedBy = openedBy; }

    public LocalDateTime getOpenedAt() { return openedAt; }
    public void setOpenedAt(LocalDateTime openedAt) { this.openedAt = openedAt; }

    public String getClosedBy() { return closedBy; }
    public void setClosedBy(String closedBy) { this.closedBy = closedBy; }

    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }

    public String getClosureNote() { return closureNote; }
    public void setClosureNote(String closureNote) { this.closureNote = closureNote; }

    public String getTriggeredFreezeId() { return triggeredFreezeId; }
    public void setTriggeredFreezeId(String triggeredFreezeId) { this.triggeredFreezeId = triggeredFreezeId; }

    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }

    public LocalDateTime getUpdatedOn() { return updatedOn; }
    public void setUpdatedOn(LocalDateTime updatedOn) { this.updatedOn = updatedOn; }

    public List<AmlAlert> getAlerts() { return alerts; }
    public void setAlerts(List<AmlAlert> alerts) { this.alerts = alerts; }

    public WalletFreeze getTriggeredFreeze() { return triggeredFreeze; }
    public void setTriggeredFreeze(WalletFreeze triggeredFreeze) { this.triggeredFreeze = triggeredFreeze; }
}
