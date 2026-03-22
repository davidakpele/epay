package com.example.admin_api_service.models.userAndWalletManagement;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.admin_api_service.enums.FreezeReason;
import com.example.admin_api_service.enums.FreezeType;
import com.example.admin_api_service.enums.WalletFreezeStatus;
import com.example.admin_api_service.models.accessAndApprovals.ApprovalRequest;

@Entity
@Table(name = "wallet_freezes")
public class WalletFreeze {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id = UUID.randomUUID().toString();

    @Column(name = "wallet_id", nullable = false)
    private Long walletId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "freeze_type", length = 30, nullable = false)
    private FreezeType freezeType;

    // FULL = no debit or credit; DEBIT_ONLY = credits still allowed; CREDIT_ONLY = debits still allowed
    @Enumerated(EnumType.STRING)
    @Column(name = "freeze_reason", length = 50, nullable = false)
    private FreezeReason freezeReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private WalletFreezeStatus status = WalletFreezeStatus.ACTIVE;

    @Column(name = "reason_note", length = 500, nullable = false)
    private String reasonNote;

    @Column(name = "internal_reference", length = 100)
    private String internalReference;

    // Linked external reference (e.g. court order number, CBN directive)
    @Column(name = "external_reference", length = 100)
    private String externalReference;

    // Linked approval request if freeze went through a workflow
    @Column(name = "approval_request_id", length = 36)
    private String approvalRequestId;

    @Column(name = "frozen_by", length = 36, nullable = false)
    private String frozenBy;

    @Column(name = "frozen_at", nullable = false)
    private LocalDateTime frozenAt = LocalDateTime.now();

    // Auto-lift date (null = indefinite)
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "unfrozen_by", length = 36)
    private String unfrozenBy;

    @Column(name = "unfrozen_at")
    private LocalDateTime unfrozenAt;

    @Column(name = "unfreeze_note", length = 500)
    private String unfreezeNote;

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

    public WalletFreeze() {
    }

    public WalletFreeze(String id, Long walletId, Long userId, FreezeType freezeType, FreezeReason freezeReason, WalletFreezeStatus status, String reasonNote, String internalReference, String externalReference, String approvalRequestId, String frozenBy, LocalDateTime frozenAt, LocalDateTime expiresAt, String unfrozenBy, LocalDateTime unfrozenAt, String unfreezeNote, String ipAddress, LocalDateTime createdOn, LocalDateTime updatedOn, ApprovalRequest approvalRequest) {
        this.id = id;
        this.walletId = walletId;
        this.userId = userId;
        this.freezeType = freezeType;
        this.freezeReason = freezeReason;
        this.status = status;
        this.reasonNote = reasonNote;
        this.internalReference = internalReference;
        this.externalReference = externalReference;
        this.approvalRequestId = approvalRequestId;
        this.frozenBy = frozenBy;
        this.frozenAt = frozenAt;
        this.expiresAt = expiresAt;
        this.unfrozenBy = unfrozenBy;
        this.unfrozenAt = unfrozenAt;
        this.unfreezeNote = unfreezeNote;
        this.ipAddress = ipAddress;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
        this.approvalRequest = approvalRequest;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Long getWalletId() { return walletId; }
    public void setWalletId(Long walletId) { this.walletId = walletId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public FreezeType getFreezeType() { return freezeType; }
    public void setFreezeType(FreezeType freezeType) { this.freezeType = freezeType; }

    public FreezeReason getFreezeReason() { return freezeReason; }
    public void setFreezeReason(FreezeReason freezeReason) { this.freezeReason = freezeReason; }

    public WalletFreezeStatus getStatus() { return status; }
    public void setStatus(WalletFreezeStatus status) { this.status = status; }

    public String getReasonNote() { return reasonNote; }
    public void setReasonNote(String reasonNote) { this.reasonNote = reasonNote; }

    public String getInternalReference() { return internalReference; }
    public void setInternalReference(String internalReference) { this.internalReference = internalReference; }

    public String getExternalReference() { return externalReference; }
    public void setExternalReference(String externalReference) { this.externalReference = externalReference; }

    public String getApprovalRequestId() { return approvalRequestId; }
    public void setApprovalRequestId(String approvalRequestId) { this.approvalRequestId = approvalRequestId; }

    public String getFrozenBy() { return frozenBy; }
    public void setFrozenBy(String frozenBy) { this.frozenBy = frozenBy; }

    public LocalDateTime getFrozenAt() { return frozenAt; }
    public void setFrozenAt(LocalDateTime frozenAt) { this.frozenAt = frozenAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public String getUnfrozenBy() { return unfrozenBy; }
    public void setUnfrozenBy(String unfrozenBy) { this.unfrozenBy = unfrozenBy; }

    public LocalDateTime getUnfrozenAt() { return unfrozenAt; }
    public void setUnfrozenAt(LocalDateTime unfrozenAt) { this.unfrozenAt = unfrozenAt; }

    public String getUnfreezeNote() { return unfreezeNote; }
    public void setUnfreezeNote(String unfreezeNote) { this.unfreezeNote = unfreezeNote; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }

    public LocalDateTime getUpdatedOn() { return updatedOn; }
    public void setUpdatedOn(LocalDateTime updatedOn) { this.updatedOn = updatedOn; }

    public ApprovalRequest getApprovalRequest() { return approvalRequest; }
    public void setApprovalRequest(ApprovalRequest approvalRequest) { this.approvalRequest = approvalRequest; }
}
