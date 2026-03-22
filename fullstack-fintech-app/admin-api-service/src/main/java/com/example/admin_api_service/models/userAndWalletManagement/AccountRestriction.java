package com.example.admin_api_service.models.userAndWalletManagement;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.admin_api_service.enums.AccountRestrictionStatus;
import com.example.admin_api_service.enums.AccountRestrictionType;
import com.example.admin_api_service.enums.RestrictionScope;
import com.example.admin_api_service.models.accessAndApprovals.ApprovalRequest;

@Entity
@Table(name = "account_restrictions")
public class AccountRestriction {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id = UUID.randomUUID().toString();

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // Optional — null means restriction applies at user level across all wallets
    @Column(name = "wallet_id")
    private Long walletId;

    @Enumerated(EnumType.STRING)
    @Column(name = "restriction_type", length = 50, nullable = false)
    private AccountRestrictionType restrictionType;

    // USER = applies to the user account; WALLET = applies to a specific wallet
    @Enumerated(EnumType.STRING)
    @Column(name = "scope", length = 20, nullable = false)
    private RestrictionScope scope = RestrictionScope.USER;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private AccountRestrictionStatus status = AccountRestrictionStatus.ACTIVE;

    @Column(name = "reason", length = 500, nullable = false)
    private String reason;

    @Column(name = "internal_note", length = 500)
    private String internalNote;

    // For transaction limit restrictions — cap applied
    @Column(name = "limit_amount", precision = 18, scale = 4)
    private BigDecimal limitAmount;

    @Column(name = "limit_currency", length = 10)
    private String limitCurrency;

    @Column(name = "external_reference", length = 100)
    private String externalReference;

    @Column(name = "approval_request_id", length = 36)
    private String approvalRequestId;

    @Column(name = "applied_by", length = 36, nullable = false)
    private String appliedBy;

    @Column(name = "applied_at", nullable = false)
    private LocalDateTime appliedAt = LocalDateTime.now();

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "lifted_by", length = 36)
    private String liftedBy;

    @Column(name = "lifted_at")
    private LocalDateTime liftedAt;

    @Column(name = "lift_note", length = 500)
    private String liftNote;

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

    public AccountRestriction() {
    }

    public AccountRestriction(String id, Long userId, Long walletId, AccountRestrictionType restrictionType, RestrictionScope scope, AccountRestrictionStatus status, String reason, String internalNote, BigDecimal limitAmount, String limitCurrency, String externalReference, String approvalRequestId, String appliedBy, LocalDateTime appliedAt, LocalDateTime expiresAt, String liftedBy, LocalDateTime liftedAt, String liftNote, String ipAddress, LocalDateTime createdOn, LocalDateTime updatedOn, ApprovalRequest approvalRequest) {
        this.id = id;
        this.userId = userId;
        this.walletId = walletId;
        this.restrictionType = restrictionType;
        this.scope = scope;
        this.status = status;
        this.reason = reason;
        this.internalNote = internalNote;
        this.limitAmount = limitAmount;
        this.limitCurrency = limitCurrency;
        this.externalReference = externalReference;
        this.approvalRequestId = approvalRequestId;
        this.appliedBy = appliedBy;
        this.appliedAt = appliedAt;
        this.expiresAt = expiresAt;
        this.liftedBy = liftedBy;
        this.liftedAt = liftedAt;
        this.liftNote = liftNote;
        this.ipAddress = ipAddress;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
        this.approvalRequest = approvalRequest;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getWalletId() { return walletId; }
    public void setWalletId(Long walletId) { this.walletId = walletId; }

    public AccountRestrictionType getRestrictionType() { return restrictionType; }
    public void setRestrictionType(AccountRestrictionType restrictionType) { this.restrictionType = restrictionType; }

    public RestrictionScope getScope() { return scope; }
    public void setScope(RestrictionScope scope) { this.scope = scope; }

    public AccountRestrictionStatus getStatus() { return status; }
    public void setStatus(AccountRestrictionStatus status) { this.status = status; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getInternalNote() { return internalNote; }
    public void setInternalNote(String internalNote) { this.internalNote = internalNote; }

    public BigDecimal getLimitAmount() { return limitAmount; }
    public void setLimitAmount(BigDecimal limitAmount) { this.limitAmount = limitAmount; }

    public String getLimitCurrency() { return limitCurrency; }
    public void setLimitCurrency(String limitCurrency) { this.limitCurrency = limitCurrency; }

    public String getExternalReference() { return externalReference; }
    public void setExternalReference(String externalReference) { this.externalReference = externalReference; }

    public String getApprovalRequestId() { return approvalRequestId; }
    public void setApprovalRequestId(String approvalRequestId) { this.approvalRequestId = approvalRequestId; }

    public String getAppliedBy() { return appliedBy; }
    public void setAppliedBy(String appliedBy) { this.appliedBy = appliedBy; }

    public LocalDateTime getAppliedAt() { return appliedAt; }
    public void setAppliedAt(LocalDateTime appliedAt) { this.appliedAt = appliedAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public String getLiftedBy() { return liftedBy; }
    public void setLiftedBy(String liftedBy) { this.liftedBy = liftedBy; }

    public LocalDateTime getLiftedAt() { return liftedAt; }
    public void setLiftedAt(LocalDateTime liftedAt) { this.liftedAt = liftedAt; }

    public String getLiftNote() { return liftNote; }
    public void setLiftNote(String liftNote) { this.liftNote = liftNote; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }

    public LocalDateTime getUpdatedOn() { return updatedOn; }
    public void setUpdatedOn(LocalDateTime updatedOn) { this.updatedOn = updatedOn; }

    public ApprovalRequest getApprovalRequest() { return approvalRequest; }
    public void setApprovalRequest(ApprovalRequest approvalRequest) { this.approvalRequest = approvalRequest; }
}
