package com.example.admin_api_service.models.systemAndConfiguration;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.admin_api_service.enums.ExchangeRateSource;

@Entity
@Table(
    name = "exchange_rate_snapshots",
    uniqueConstraints = @UniqueConstraint(columnNames = {"base_currency", "target_currency", "effective_at"})
)
public class ExchangeRateSnapshot {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id = UUID.randomUUID().toString();

    @Column(name = "base_currency", length = 10, nullable = false)
    private String baseCurrency;

    @Column(name = "target_currency", length = 10, nullable = false)
    private String targetCurrency;

    // Mid-market rate
    @Column(name = "rate", precision = 18, scale = 8, nullable = false)
    private BigDecimal rate;

    // Buy rate (what we pay to acquire the foreign currency)
    @Column(name = "buy_rate", precision = 18, scale = 8)
    private BigDecimal buyRate;

    // Sell rate (what customers pay to convert)
    @Column(name = "sell_rate", precision = 18, scale = 8)
    private BigDecimal sellRate;

    // Spread applied on top of mid-market rate (as a percentage e.g. 0.015 = 1.5%)
    @Column(name = "spread_percentage", precision = 8, scale = 6)
    private BigDecimal spreadPercentage;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", length = 30, nullable = false)
    private ExchangeRateSource source;

    // Provider reference or API response ID
    @Column(name = "provider_reference", length = 100)
    private String providerReference;

    // Whether this rate is currently active for transactions
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    // Whether this rate was manually overridden by an admin
    @Column(name = "is_manual_override", nullable = false)
    private boolean isManualOverride = false;

    @Column(name = "overridden_by", length = 36)
    private String overriddenBy;

    @Column(name = "override_reason", length = 500)
    private String overrideReason;

    // When this rate is valid from
    @Column(name = "effective_at", nullable = false)
    private LocalDateTime effectiveAt;

    // When this rate expires — null means no expiry
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn = LocalDateTime.now();

    public ExchangeRateSnapshot() {
    }

    public ExchangeRateSnapshot(String id, String baseCurrency, String targetCurrency, BigDecimal rate, BigDecimal buyRate, BigDecimal sellRate, BigDecimal spreadPercentage, ExchangeRateSource source, String providerReference, boolean isActive, boolean isManualOverride, String overriddenBy, String overrideReason, LocalDateTime effectiveAt, LocalDateTime expiresAt, LocalDateTime createdOn) {
        this.id = id;
        this.baseCurrency = baseCurrency;
        this.targetCurrency = targetCurrency;
        this.rate = rate;
        this.buyRate = buyRate;
        this.sellRate = sellRate;
        this.spreadPercentage = spreadPercentage;
        this.source = source;
        this.providerReference = providerReference;
        this.isActive = isActive;
        this.isManualOverride = isManualOverride;
        this.overriddenBy = overriddenBy;
        this.overrideReason = overrideReason;
        this.effectiveAt = effectiveAt;
        this.expiresAt = expiresAt;
        this.createdOn = createdOn;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBaseCurrency() { return baseCurrency; }
    public void setBaseCurrency(String baseCurrency) { this.baseCurrency = baseCurrency; }

    public String getTargetCurrency() { return targetCurrency; }
    public void setTargetCurrency(String targetCurrency) { this.targetCurrency = targetCurrency; }

    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal rate) { this.rate = rate; }

    public BigDecimal getBuyRate() { return buyRate; }
    public void setBuyRate(BigDecimal buyRate) { this.buyRate = buyRate; }

    public BigDecimal getSellRate() { return sellRate; }
    public void setSellRate(BigDecimal sellRate) { this.sellRate = sellRate; }

    public BigDecimal getSpreadPercentage() { return spreadPercentage; }
    public void setSpreadPercentage(BigDecimal spreadPercentage) { this.spreadPercentage = spreadPercentage; }

    public ExchangeRateSource getSource() { return source; }
    public void setSource(ExchangeRateSource source) { this.source = source; }

    public String getProviderReference() { return providerReference; }
    public void setProviderReference(String providerReference) { this.providerReference = providerReference; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public boolean isManualOverride() { return isManualOverride; }
    public void setManualOverride(boolean manualOverride) { isManualOverride = manualOverride; }

    public String getOverriddenBy() { return overriddenBy; }
    public void setOverriddenBy(String overriddenBy) { this.overriddenBy = overriddenBy; }

    public String getOverrideReason() { return overrideReason; }
    public void setOverrideReason(String overrideReason) { this.overrideReason = overrideReason; }

    public LocalDateTime getEffectiveAt() { return effectiveAt; }
    public void setEffectiveAt(LocalDateTime effectiveAt) { this.effectiveAt = effectiveAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }
}