package com.example.admin_api_service.models.feeAndLimits;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import com.example.admin_api_service.enums.FeeCalculationMethod;

@Entity
@Table(name = "fee_rules")
public class FeeRule {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id = UUID.randomUUID().toString();

    @Column(name = "fee_configuration_id", length = 36, nullable = false)
    private String feeConfigurationId;

    // Lower bound of the transaction amount range this rule applies to (inclusive)
    @Column(name = "min_amount", precision = 18, scale = 4, nullable = false)
    private BigDecimal minAmount;

    // Upper bound of the transaction amount range — null means no upper cap
    @Column(name = "max_amount", precision = 18, scale = 4)
    private BigDecimal maxAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "calculation_method", length = 20, nullable = false)
    private FeeCalculationMethod calculationMethod;

    // Flat fee amount (used when calculationMethod = FLAT or FLAT_PLUS_PERCENTAGE)
    @Column(name = "flat_amount", precision = 18, scale = 4)
    private BigDecimal flatAmount;

    // Percentage rate (used when calculationMethod = PERCENTAGE or FLAT_PLUS_PERCENTAGE)
    // e.g. 0.015 = 1.5%
    @Column(name = "percentage_rate", precision = 8, scale = 6)
    private BigDecimal percentageRate;

    // Minimum fee to charge regardless of calculation result
    @Column(name = "min_fee", precision = 18, scale = 4)
    private BigDecimal minFee;

    // Maximum fee cap regardless of calculation result — null means no cap
    @Column(name = "max_fee", precision = 18, scale = 4)
    private BigDecimal maxFee;

    // Whether this fee is waived (zero fee) for this band
    @Column(name = "is_waived", nullable = false)
    private boolean isWaived = false;

    @Column(name = "created_by", length = 36)
    private String createdBy;

    @Column(name = "updated_by", length = 36)
    private String updatedBy;

    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn = LocalDateTime.now();

    @Column(name = "updated_on", nullable = false)
    private LocalDateTime updatedOn = LocalDateTime.now();

    // Navigation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fee_configuration_id", insertable = false, updatable = false)
    private FeeConfiguration feeConfiguration;

    @PreUpdate
    public void preUpdate() {
        this.updatedOn = LocalDateTime.now();
    }

    public FeeRule() {
    }

    public FeeRule(String id, String feeConfigurationId, BigDecimal minAmount, BigDecimal maxAmount, FeeCalculationMethod calculationMethod, BigDecimal flatAmount, BigDecimal percentageRate, BigDecimal minFee, BigDecimal maxFee, boolean isWaived, String createdBy, String updatedBy, LocalDateTime createdOn, LocalDateTime updatedOn, FeeConfiguration feeConfiguration) {
        this.id = id;
        this.feeConfigurationId = feeConfigurationId;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.calculationMethod = calculationMethod;
        this.flatAmount = flatAmount;
        this.percentageRate = percentageRate;
        this.minFee = minFee;
        this.maxFee = maxFee;
        this.isWaived = isWaived;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
        this.feeConfiguration = feeConfiguration;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFeeConfigurationId() { return feeConfigurationId; }
    public void setFeeConfigurationId(String feeConfigurationId) { this.feeConfigurationId = feeConfigurationId; }

    public BigDecimal getMinAmount() { return minAmount; }
    public void setMinAmount(BigDecimal minAmount) { this.minAmount = minAmount; }

    public BigDecimal getMaxAmount() { return maxAmount; }
    public void setMaxAmount(BigDecimal maxAmount) { this.maxAmount = maxAmount; }

    public FeeCalculationMethod getCalculationMethod() { return calculationMethod; }
    public void setCalculationMethod(FeeCalculationMethod calculationMethod) { this.calculationMethod = calculationMethod; }

    public BigDecimal getFlatAmount() { return flatAmount; }
    public void setFlatAmount(BigDecimal flatAmount) { this.flatAmount = flatAmount; }

    public BigDecimal getPercentageRate() { return percentageRate; }
    public void setPercentageRate(BigDecimal percentageRate) { this.percentageRate = percentageRate; }

    public BigDecimal getMinFee() { return minFee; }
    public void setMinFee(BigDecimal minFee) { this.minFee = minFee; }

    public BigDecimal getMaxFee() { return maxFee; }
    public void setMaxFee(BigDecimal maxFee) { this.maxFee = maxFee; }

    public boolean isWaived() { return isWaived; }
    public void setWaived(boolean waived) { isWaived = waived; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }

    public LocalDateTime getUpdatedOn() { return updatedOn; }
    public void setUpdatedOn(LocalDateTime updatedOn) { this.updatedOn = updatedOn; }

    public FeeConfiguration getFeeConfiguration() { return feeConfiguration; }
    public void setFeeConfiguration(FeeConfiguration feeConfiguration) { this.feeConfiguration = feeConfiguration; }
}
