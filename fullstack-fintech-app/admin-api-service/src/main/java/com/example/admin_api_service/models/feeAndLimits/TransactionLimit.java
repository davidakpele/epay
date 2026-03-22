package com.example.admin_api_service.models.feeAndLimits;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.admin_api_service.enums.TransactionLimitScope;
import com.example.admin_api_service.enums.TransactionLimitStatus;
import com.example.admin_api_service.enums.TransactionLimitType;

@Entity
@Table(name = "transaction_limits")
public class TransactionLimit {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id = UUID.randomUUID().toString();

    // Unique machine-readable code e.g. "TIER1_TRANSFER_DAILY_NGN"
    @Column(name = "code", length = 100, nullable = false, unique = true)
    private String code;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "limit_type", length = 30, nullable = false)
    private TransactionLimitType limitType;

    // GLOBAL = applies to all users; KYC_TIER = applies per tier; USER = user-level override
    @Enumerated(EnumType.STRING)
    @Column(name = "scope", length = 20, nullable = false)
    private TransactionLimitScope scope = TransactionLimitScope.GLOBAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private TransactionLimitStatus status = TransactionLimitStatus.ACTIVE;

    // Transaction type this limit applies to — null means all transaction types
    @Column(name = "transaction_type", length = 50)
    private String transactionType;

    // Channel this limit applies to — null means all channels
    @Column(name = "channel", length = 20)
    private String channel;

    // KYC tier this limit applies to — null when scope is GLOBAL
    @Column(name = "kyc_tier", length = 10)
    private String kycTier;

    @Column(name = "currency", length = 10, nullable = false)
    private String currency;

    // Maximum amount per single transaction
    @Column(name = "single_transaction_max", precision = 18, scale = 4)
    private BigDecimal singleTransactionMax;

    // Minimum amount per single transaction
    @Column(name = "single_transaction_min", precision = 18, scale = 4)
    private BigDecimal singleTransactionMin;

    // Cumulative daily limit
    @Column(name = "daily_max", precision = 18, scale = 4)
    private BigDecimal dailyMax;

    // Maximum number of transactions per day
    @Column(name = "daily_count_max")
    private Integer dailyCountMax;

    // Cumulative weekly limit
    @Column(name = "weekly_max", precision = 18, scale = 4)
    private BigDecimal weeklyMax;

    // Maximum number of transactions per week
    @Column(name = "weekly_count_max")
    private Integer weeklyCountMax;

    // Cumulative monthly limit
    @Column(name = "monthly_max", precision = 18, scale = 4)
    private BigDecimal monthlyMax;

    // Maximum number of transactions per month
    @Column(name = "monthly_count_max")
    private Integer monthlyCountMax;

    @Column(name = "effective_from", nullable = false)
    private LocalDateTime effectiveFrom;

    @Column(name = "effective_to")
    private LocalDateTime effectiveTo;

    @Column(name = "created_by", length = 36)
    private String createdBy;

    @Column(name = "updated_by", length = 36)
    private String updatedBy;

    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn = LocalDateTime.now();

    @Column(name = "updated_on", nullable = false)
    private LocalDateTime updatedOn = LocalDateTime.now();

    // Navigation
    @OneToMany(mappedBy = "transactionLimit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransactionLimitOverride> overrides = new ArrayList<>();

    @PreUpdate
    public void preUpdate() {
        this.updatedOn = LocalDateTime.now();
    }

    public TransactionLimit() {
    }

    public TransactionLimit(String id, String code, String name, String description, TransactionLimitType limitType, TransactionLimitScope scope, TransactionLimitStatus status, String transactionType, String channel, String kycTier, String currency, BigDecimal singleTransactionMax, BigDecimal singleTransactionMin, BigDecimal dailyMax, Integer dailyCountMax, BigDecimal weeklyMax, Integer weeklyCountMax, BigDecimal monthlyMax, Integer monthlyCountMax, LocalDateTime effectiveFrom, LocalDateTime effectiveTo, String createdBy, String updatedBy, LocalDateTime createdOn, LocalDateTime updatedOn, List<TransactionLimitOverride> overrides) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.limitType = limitType;
        this.scope = scope;
        this.status = status;
        this.transactionType = transactionType;
        this.channel = channel;
        this.kycTier = kycTier;
        this.currency = currency;
        this.singleTransactionMax = singleTransactionMax;
        this.singleTransactionMin = singleTransactionMin;
        this.dailyMax = dailyMax;
        this.dailyCountMax = dailyCountMax;
        this.weeklyMax = weeklyMax;
        this.weeklyCountMax = weeklyCountMax;
        this.monthlyMax = monthlyMax;
        this.monthlyCountMax = monthlyCountMax;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
        this.overrides = overrides;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TransactionLimitType getLimitType() { return limitType; }
    public void setLimitType(TransactionLimitType limitType) { this.limitType = limitType; }

    public TransactionLimitScope getScope() { return scope; }
    public void setScope(TransactionLimitScope scope) { this.scope = scope; }

    public TransactionLimitStatus getStatus() { return status; }
    public void setStatus(TransactionLimitStatus status) { this.status = status; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }

    public String getKycTier() { return kycTier; }
    public void setKycTier(String kycTier) { this.kycTier = kycTier; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

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

    public LocalDateTime getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(LocalDateTime effectiveFrom) { this.effectiveFrom = effectiveFrom; }

    public LocalDateTime getEffectiveTo() { return effectiveTo; }
    public void setEffectiveTo(LocalDateTime effectiveTo) { this.effectiveTo = effectiveTo; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }

    public LocalDateTime getUpdatedOn() { return updatedOn; }
    public void setUpdatedOn(LocalDateTime updatedOn) { this.updatedOn = updatedOn; }

    public List<TransactionLimitOverride> getOverrides() { return overrides; }
    public void setOverrides(List<TransactionLimitOverride> overrides) { this.overrides = overrides; }
}
