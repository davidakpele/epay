package com.example.admin_api_service.models.accessAndApprovals;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.admin_api_service.enums.AdjustmentType;
import com.example.admin_api_service.enums.ManualAdjustmentStatus;

@Entity
@Table(name = "manual_adjustment_requests")
public class ManualAdjustmentRequest {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id = UUID.randomUUID().toString();

    // Linked approval request if this went through a workflow
    @Column(name = "approval_request_id", length = 36)
    private String approvalRequestId;

    @Column(name = "wallet_id", nullable = false)
    private Long walletId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "adjustment_type", length = 30, nullable = false)
    private AdjustmentType adjustmentType;

    @Column(name = "amount", precision = 18, scale = 4, nullable = false)
    private BigDecimal amount;

    @Column(name = "currency", length = 10, nullable = false)
    private String currency;

    // Balance snapshot before adjustment
    @Column(name = "balance_before", precision = 18, scale = 4, nullable = false)
    private BigDecimal balanceBefore;

    // Balance snapshot after adjustment (set on execution)
    @Column(name = "balance_after", precision = 18, scale = 4)
    private BigDecimal balanceAfter;

    @Column(name = "reason", length = 500, nullable = false)
    private String reason;

    @Column(name = "internal_note", length = 500)
    private String internalNote;

    // Reference to the resulting history/ledger entry
    @Column(name = "history_reference_id", length = 36)
    private String historyReferenceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private ManualAdjustmentStatus status = ManualAdjustmentStatus.PENDING;

    @Column(name = "requested_by", length = 36, nullable = false)
    private String requestedBy;

    @Column(name = "approved_by", length = 36)
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejected_by", length = 36)
    private String rejectedBy;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn = LocalDateTime.now();

    @Column(name = "updated_on", nullable = false)
    private LocalDateTime updatedOn = LocalDateTime.now();

    // Navigation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approval_request_id", insertable = false, updatable = false)
    private ApprovalRequest approvalRequest;

    @PreUpdate
    public void preUpdate() {
        this.updatedOn = LocalDateTime.now();
    }

    public ManualAdjustmentRequest() {
    }

    public ManualAdjustmentRequest(String id, String approvalRequestId, Long walletId, Long userId, AdjustmentType adjustmentType, BigDecimal amount, String currency, BigDecimal balanceBefore, BigDecimal balanceAfter, String reason, String internalNote, String historyReferenceId, ManualAdjustmentStatus status, String requestedBy, String approvedBy, LocalDateTime approvedAt, String rejectedBy, String rejectionReason, LocalDateTime rejectedAt, LocalDateTime executedAt, String ipAddress, LocalDateTime createdOn, LocalDateTime updatedOn, ApprovalRequest approvalRequest) {
        this.id = id;
        this.approvalRequestId = approvalRequestId;
        this.walletId = walletId;
        this.userId = userId;
        this.adjustmentType = adjustmentType;
        this.amount = amount;
        this.currency = currency;
        this.balanceBefore = balanceBefore;
        this.balanceAfter = balanceAfter;
        this.reason = reason;
        this.internalNote = internalNote;
        this.historyReferenceId = historyReferenceId;
        this.status = status;
        this.requestedBy = requestedBy;
        this.approvedBy = approvedBy;
        this.approvedAt = approvedAt;
        this.rejectedBy = rejectedBy;
        this.rejectionReason = rejectionReason;
        this.rejectedAt = rejectedAt;
        this.executedAt = executedAt;
        this.ipAddress = ipAddress;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
        this.approvalRequest = approvalRequest;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getApprovalRequestId() { return approvalRequestId; }
    public void setApprovalRequestId(String approvalRequestId) { this.approvalRequestId = approvalRequestId; }

    public Long getWalletId() { return walletId; }
    public void setWalletId(Long walletId) { this.walletId = walletId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public AdjustmentType getAdjustmentType() { return adjustmentType; }
    public void setAdjustmentType(AdjustmentType adjustmentType) { this.adjustmentType = adjustmentType; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getBalanceBefore() { return balanceBefore; }
    public void setBalanceBefore(BigDecimal balanceBefore) { this.balanceBefore = balanceBefore; }

    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(BigDecimal balanceAfter) { this.balanceAfter = balanceAfter; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getInternalNote() { return internalNote; }
    public void setInternalNote(String internalNote) { this.internalNote = internalNote; }

    public String getHistoryReferenceId() { return historyReferenceId; }
    public void setHistoryReferenceId(String historyReferenceId) { this.historyReferenceId = historyReferenceId; }

    public ManualAdjustmentStatus getStatus() { return status; }
    public void setStatus(ManualAdjustmentStatus status) { this.status = status; }

    public String getRequestedBy() { return requestedBy; }
    public void setRequestedBy(String requestedBy) { this.requestedBy = requestedBy; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }

    public String getRejectedBy() { return rejectedBy; }
    public void setRejectedBy(String rejectedBy) { this.rejectedBy = rejectedBy; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public LocalDateTime getRejectedAt() { return rejectedAt; }
    public void setRejectedAt(LocalDateTime rejectedAt) { this.rejectedAt = rejectedAt; }

    public LocalDateTime getExecutedAt() { return executedAt; }
    public void setExecutedAt(LocalDateTime executedAt) { this.executedAt = executedAt; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }

    public LocalDateTime getUpdatedOn() { return updatedOn; }
    public void setUpdatedOn(LocalDateTime updatedOn) { this.updatedOn = updatedOn; }

    public ApprovalRequest getApprovalRequest() { return approvalRequest; }
    public void setApprovalRequest(ApprovalRequest approvalRequest) { this.approvalRequest = approvalRequest; }
}