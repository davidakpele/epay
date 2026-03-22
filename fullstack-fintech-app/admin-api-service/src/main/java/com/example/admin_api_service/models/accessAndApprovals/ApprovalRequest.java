package com.example.admin_api_service.models.accessAndApprovals;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.admin_api_service.enums.ApprovalRequestStatus;
import com.example.admin_api_service.enums.ApprovalTargetType;

@Entity
@Table(name = "approval_requests")
public class ApprovalRequest {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id = UUID.randomUUID().toString();

    @Column(name = "workflow_id", length = 36, nullable = false)
    private String workflowId;

    // The step currently awaiting action
    @Column(name = "current_step_id", length = 36)
    private String currentStepId;

    // What entity this request is acting on
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", length = 50, nullable = false)
    private ApprovalTargetType targetType;

    // ID of the entity being acted on (walletId, userId, transactionId, etc.)
    @Column(name = "target_id", length = 36, nullable = false)
    private String targetId;

    // Snapshot of the amount involved (for threshold checks and audit)
    @Column(name = "amount", precision = 18, scale = 4)
    private BigDecimal amount;

    @Column(name = "currency", length = 10)
    private String currency;

    // Full context of the requested action stored as JSON
    @Column(name = "payload", columnDefinition = "json")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private ApprovalRequestStatus status = ApprovalRequestStatus.PENDING;

    @Column(name = "requested_by", length = 36, nullable = false)
    private String requestedBy;

    @Column(name = "request_note", length = 500)
    private String requestNote;

    // Admin who took the final action (approved/rejected)
    @Column(name = "resolved_by", length = 36)
    private String resolvedBy;

    @Column(name = "resolved_note", length = 500)
    private String resolvedNote;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn = LocalDateTime.now();

    @Column(name = "updated_on", nullable = false)
    private LocalDateTime updatedOn = LocalDateTime.now();

    // Navigation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", insertable = false, updatable = false)
    private ApprovalWorkflow workflow;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_step_id", insertable = false, updatable = false)
    private ApprovalStep currentStep;

    @PreUpdate
    public void preUpdate() {
        this.updatedOn = LocalDateTime.now();
    }

    public ApprovalRequest() {
    }

    public ApprovalRequest(String id, String workflowId, String currentStepId, ApprovalTargetType targetType, String targetId, BigDecimal amount, String currency, String payload, ApprovalRequestStatus status, String requestedBy, String requestNote, String resolvedBy, String resolvedNote, LocalDateTime resolvedAt, LocalDateTime expiresAt, LocalDateTime createdOn, LocalDateTime updatedOn, ApprovalWorkflow workflow, ApprovalStep currentStep) {
        this.id = id;
        this.workflowId = workflowId;
        this.currentStepId = currentStepId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.amount = amount;
        this.currency = currency;
        this.payload = payload;
        this.status = status;
        this.requestedBy = requestedBy;
        this.requestNote = requestNote;
        this.resolvedBy = resolvedBy;
        this.resolvedNote = resolvedNote;
        this.resolvedAt = resolvedAt;
        this.expiresAt = expiresAt;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
        this.workflow = workflow;
        this.currentStep = currentStep;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getWorkflowId() { return workflowId; }
    public void setWorkflowId(String workflowId) { this.workflowId = workflowId; }

    public String getCurrentStepId() { return currentStepId; }
    public void setCurrentStepId(String currentStepId) { this.currentStepId = currentStepId; }

    public ApprovalTargetType getTargetType() { return targetType; }
    public void setTargetType(ApprovalTargetType targetType) { this.targetType = targetType; }

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    public ApprovalRequestStatus getStatus() { return status; }
    public void setStatus(ApprovalRequestStatus status) { this.status = status; }

    public String getRequestedBy() { return requestedBy; }
    public void setRequestedBy(String requestedBy) { this.requestedBy = requestedBy; }

    public String getRequestNote() { return requestNote; }
    public void setRequestNote(String requestNote) { this.requestNote = requestNote; }

    public String getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }

    public String getResolvedNote() { return resolvedNote; }
    public void setResolvedNote(String resolvedNote) { this.resolvedNote = resolvedNote; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }

    public LocalDateTime getUpdatedOn() { return updatedOn; }
    public void setUpdatedOn(LocalDateTime updatedOn) { this.updatedOn = updatedOn; }

    public ApprovalWorkflow getWorkflow() { return workflow; }
    public void setWorkflow(ApprovalWorkflow workflow) { this.workflow = workflow; }

    public ApprovalStep getCurrentStep() { return currentStep; }
    public void setCurrentStep(ApprovalStep currentStep) { this.currentStep = currentStep; }
}
