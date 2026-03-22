package com.example.admin_api_service.models.feeAndLimits;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import com.example.admin_api_service.enums.LimitOverrideStatus;

@Entity
@Table(name = "transaction_limit_overrides")
public class TransactionLimitOverride {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id = UUID.randomUUID().toString();

    @Column(name = "transaction_limit_id", length = 36, nullable = false)
    private String transactionLimitId;

    // The specific user this override applies to
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "wallet_id")
    private Long walletId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private LimitOverrideStatus status = LimitOverrideStatus.ACTIVE;

    @Column(name = "reason", length = 500, nullable = false)
    private String reason;

    @Column(name = "internal_note", length = 500)
    private String internalNote;

    // Overridden values — null means inherit from the base TransactionLimit
    @Column(name = "single_transaction_max", precision = 18, scale = 4)
    private BigDecimal singleTransactionMax;

    @Column(name = "single_transaction_min", precision = 18, scale = 4)
    private BigDecimal singleTransactionMin;

    @Column(name = "daily_max", precision = 18, scale = 4)
    private BigDecimal dailyMax;

    @Column(name = "daily_count_max")
    private Integer dailyCountMax;

    @Column(name = "weekly_max", precision = 18, scale = 4)
    private BigDecimal weeklyMax;

    @Column(name = "weekly_count_max")
    private Integer weeklyCountMax;

    @Column(name = "monthly_max", precision = 18, scale = 4)
    private BigDecimal monthlyMax;

    @Column(name = "monthly_count_max")
    private Integer monthlyCountMax;

    // Linked approval request if this override required sign-off
    @Column(name = "approval_request_id", length = 36)
    private String approvalRequestId;

    @Column(name = "approved_by", length = 36)
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "applied_by", length = 36, nullable = false)
    private String appliedBy;

    @Column(name = "applied_at", nullable = false)
    private LocalDateTime appliedAt = LocalDateTime.now();

    // Auto-expiry — null means indefinite
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "revoked_by", length = 36)
    private String revokedBy;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "revoke_reason", length = 500)
    private String revokeReason;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn = LocalDateTime.now();

    @Column(name = "updated_on", nullable = false)
    private LocalDateTime updatedOn = LocalDateTime.now();

    // Navigation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_limit_id", insertable = false, updatable = false)
    private TransactionLimit transactionLimit;

    @PreUpdate
    public void preUpdate() {
        this.updatedOn = LocalDateTime.now();
    }

    public TransactionLimitOverride() {
    }

    public TransactionLimitOverride(String id, String transactionLimitId, Long userId, Long walletId, LimitOverrideStatus status, String reason, String internalNote, BigDecimal singleTransactionMax, BigDecimal singleTransactionMin, BigDecimal dailyMax, Integer dailyCountMax, BigDecimal weeklyMax, Integer weeklyCountMax, BigDecimal monthlyMax, Integer monthlyCountMax, String approvalRequestId, String approvedBy, LocalDateTime approvedAt, String appliedBy, LocalDateTime appliedAt, LocalDateTime expiresAt, String revokedBy, LocalDateTime revokedAt, String revokeReason, String ipAddress, LocalDateTime createdOn, LocalDateTime updatedOn, TransactionLimit transactionLimit) {
        this.id = id;
        this.transactionLimitId = transactionLimitId;
        this.userId = userId;
        this.walletId = walletId;
        this.status = status;
        this.reason = reason;
        this.internalNote = internalNote;
        this.singleTransactionMax = singleTransactionMax;
        this.singleTransactionMin = singleTransactionMin;
        this.dailyMax = dailyMax;
        this.dailyCountMax = dailyCountMax;
        this.weeklyMax = weeklyMax;
        this.weeklyCountMax = weeklyCountMax;
        this.monthlyMax = monthlyMax;
        this.monthlyCountMax = monthlyCountMax;
        this.approvalRequestId = approvalRequestId;
        this.approvedBy = approvedBy;
        this.approvedAt = approvedAt;
        this.appliedBy = appliedBy;
        this.appliedAt = appliedAt;
        this.expiresAt = expiresAt;
        this.revokedBy = revokedBy;
        this.revokedAt = revokedAt;
        this.revokeReason = revokeReason;
        this.ipAddress = ipAddress;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
        this.transactionLimit = transactionLimit;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTransactionLimitId() { return transactionLimitId; }
    public void setTransactionLimitId(String transactionLimitId) { this.transactionLimitId = transactionLimitId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getWalletId() { return walletId; }
    public void setWalletId(Long walletId) { this.walletId = walletId; }

    public LimitOverrideStatus getStatus() { return status; }
    public void setStatus(LimitOverrideStatus status) { this.status = status; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getInternalNote() { return internalNote; }
    public void setInternalNote(String internalNote) { this.internalNote = internalNote; }

    public BigDecimal getSingleTransactionMax() { return singleTransactionMax; }
    public void setSingleTransactionMax(BigDecimal singleTransactionMax) { this.singleTransactionMax = singleTransactionMax; }

    public BigDecimal getSingleTransactionMin() { return singleTransactionMin; }
    public void setSingleTransactionMin(BigDecimal singleTransactionMin) { this.singleTransactionMin = singleTransactionMin; }

    public BigDecimal getDailyMax() { return dailyMax; }
    public void setDailyMax(BigDecimal dailyMax) { this.dailyMax = dailyMax; }

    public Integer getDailyCountMax() { return dailyCountMax; }
    public void setDailyCountMax(Integer dailyCountMax) { this.dailyCountMax = dailyCountMax; }

    public BigDecimal getWeeklyMax() { return weeklyMax; }
    public void setWeeklyMax(BigDecimal weeklyMax) { this.weeklyMax = weeklyMax; }

    public Integer getWeeklyCountMax() { return weeklyCountMax; }
    public void setWeeklyCountMax(Integer weeklyCountMax) { this.weeklyCountMax = weeklyCountMax; }

    public BigDecimal getMonthlyMax() { return monthlyMax; }
    public void setMonthlyMax(BigDecimal monthlyMax) { this.monthlyMax = monthlyMax; }

    public Integer getMonthlyCountMax() { return monthlyCountMax; }
    public void setMonthlyCountMax(Integer monthlyCountMax) { this.monthlyCountMax = monthlyCountMax; }

    public String getApprovalRequestId() { return approvalRequestId; }
    public void setApprovalRequestId(String approvalRequestId) { this.approvalRequestId = approvalRequestId; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }

    public String getAppliedBy() { return appliedBy; }
    public void setAppliedBy(String appliedBy) { this.appliedBy = appliedBy; }

    public LocalDateTime getAppliedAt() { return appliedAt; }
    public void setAppliedAt(LocalDateTime appliedAt) { this.appliedAt = appliedAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public String getRevokedBy() { return revokedBy; }
    public void setRevokedBy(String revokedBy) { this.revokedBy = revokedBy; }

    public LocalDateTime getRevokedAt() { return revokedAt; }
    public void setRevokedAt(LocalDateTime revokedAt) { this.revokedAt = revokedAt; }

    public String getRevokeReason() { return revokeReason; }
    public void setRevokeReason(String revokeReason) { this.revokeReason = revokeReason; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }

    public LocalDateTime getUpdatedOn() { return updatedOn; }
    public void setUpdatedOn(LocalDateTime updatedOn) { this.updatedOn = updatedOn; }

    public TransactionLimit getTransactionLimit() { return transactionLimit; }
    public void setTransactionLimit(TransactionLimit transactionLimit) { this.transactionLimit = transactionLimit; }
}