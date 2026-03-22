package com.example.admin_api_service.models.feeAndLimits;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.admin_api_service.enums.FeeConfigurationStatus;
import com.example.admin_api_service.enums.FeeConfigurationType;

@Entity
@Table(name = "fee_configurations")
public class FeeConfiguration {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id = UUID.randomUUID().toString();

    // Unique machine-readable code e.g. "TRANSFER_OUTBOUND_NGN"
    @Column(name = "code", length = 100, nullable = false, unique = true)
    private String code;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 50, nullable = false)
    private FeeConfigurationType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private FeeConfigurationStatus status = FeeConfigurationStatus.ACTIVE;

    // Transaction type this config applies to (maps to TransactionType enum value)
    @Column(name = "transaction_type", length = 50, nullable = false)
    private String transactionType;

    // Channel this config applies to — null means all channels
    @Column(name = "channel", length = 20)
    private String channel;

    // Currency this config applies to
    @Column(name = "currency", length = 10, nullable = false)
    private String currency;

    // KYC tier this config applies to — null means all tiers
    @Column(name = "kyc_tier", length = 10)
    private String kycTier;

    // Whether fees collected go to a specific system wallet
    @Column(name = "fee_collection_wallet_id")
    private Long feeCollectionWalletId;

    // Whether VAT/tax is applied on top of the fee
    @Column(name = "vat_applicable", nullable = false)
    private boolean vatApplicable = false;

    @Column(name = "vat_rate", precision = 5, scale = 4)
    private java.math.BigDecimal vatRate;

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
    @OneToMany(mappedBy = "feeConfiguration", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("minAmount ASC")
    private List<FeeRule> feeRules = new ArrayList<>();

    @PreUpdate
    public void preUpdate() {
        this.updatedOn = LocalDateTime.now();
    }

    public FeeConfiguration() {
    }

    public FeeConfiguration(String id, String code, String name, String description, FeeConfigurationType type, FeeConfigurationStatus status, String transactionType, String channel, String currency, String kycTier, Long feeCollectionWalletId, boolean vatApplicable, java.math.BigDecimal vatRate, LocalDateTime effectiveFrom, LocalDateTime effectiveTo, String createdBy, String updatedBy, LocalDateTime createdOn, LocalDateTime updatedOn, List<FeeRule> feeRules) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.type = type;
        this.status = status;
        this.transactionType = transactionType;
        this.channel = channel;
        this.currency = currency;
        this.kycTier = kycTier;
        this.feeCollectionWalletId = feeCollectionWalletId;
        this.vatApplicable = vatApplicable;
        this.vatRate = vatRate;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
        this.feeRules = feeRules;
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

    public FeeConfigurationType getType() { return type; }
    public void setType(FeeConfigurationType type) { this.type = type; }

    public FeeConfigurationStatus getStatus() { return status; }
    public void setStatus(FeeConfigurationStatus status) { this.status = status; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getKycTier() { return kycTier; }
    public void setKycTier(String kycTier) { this.kycTier = kycTier; }

    public Long getFeeCollectionWalletId() { return feeCollectionWalletId; }
    public void setFeeCollectionWalletId(Long feeCollectionWalletId) { this.feeCollectionWalletId = feeCollectionWalletId; }

    public boolean isVatApplicable() { return vatApplicable; }
    public void setVatApplicable(boolean vatApplicable) { this.vatApplicable = vatApplicable; }

    public java.math.BigDecimal getVatRate() { return vatRate; }
    public void setVatRate(java.math.BigDecimal vatRate) { this.vatRate = vatRate; }

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

    public List<FeeRule> getFeeRules() { return feeRules; }
    public void setFeeRules(List<FeeRule> feeRules) { this.feeRules = feeRules; }
}