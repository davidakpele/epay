package com.example.admin_api_service.models.systemAndConfiguration;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.admin_api_service.enums.CurrencyStatus;
import com.example.admin_api_service.enums.CurrencyType;

@Entity
@Table(name = "supported_currencies")
public class SupportedCurrency {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id = UUID.randomUUID().toString();

    // ISO 4217 code e.g. NGN, USD, GBP
    @Column(name = "code", length = 10, nullable = false, unique = true)
    private String code;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "symbol", length = 10, nullable = false)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency_type", length = 20, nullable = false)
    private CurrencyType currencyType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private CurrencyStatus status = CurrencyStatus.ACTIVE;

    // Number of decimal places e.g. 2 for NGN, 0 for JPY
    @Column(name = "decimal_places", nullable = false)
    private int decimalPlaces = 2;

    // Smallest unit of the currency in minor units e.g. 1 kobo for NGN
    @Column(name = "minor_unit_name", length = 50)
    private String minorUnitName;

    // Whether this is the platform's base/primary currency
    @Column(name = "is_base_currency", nullable = false)
    private boolean isBaseCurrency = false;

    // Whether wallets can be created in this currency
    @Column(name = "wallet_enabled", nullable = false)
    private boolean walletEnabled = true;

    // Whether this currency can be used for incoming transfers
    @Column(name = "deposit_enabled", nullable = false)
    private boolean depositEnabled = true;

    // Whether this currency can be withdrawn
    @Column(name = "withdrawal_enabled", nullable = false)
    private boolean withdrawalEnabled = true;

    // Whether FX conversion is supported to/from this currency
    @Column(name = "fx_enabled", nullable = false)
    private boolean fxEnabled = false;

    // Minimum wallet balance allowed
    @Column(name = "minimum_balance", precision = 18, scale = 4)
    private BigDecimal minimumBalance;

    // Country this currency is associated with (ISO 3166-1 alpha-2)
    @Column(name = "country_code", length = 5)
    private String countryCode;

    @Column(name = "updated_by", length = 36)
    private String updatedBy;

    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn = LocalDateTime.now();

    @Column(name = "updated_on", nullable = false)
    private LocalDateTime updatedOn = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() { this.updatedOn = LocalDateTime.now(); }

    public SupportedCurrency() {
    }

    public SupportedCurrency(String id, String code, String name, String symbol, CurrencyType currencyType, CurrencyStatus status, int decimalPlaces, String minorUnitName, boolean isBaseCurrency, boolean walletEnabled, boolean depositEnabled, boolean withdrawalEnabled, boolean fxEnabled, BigDecimal minimumBalance, String countryCode, String updatedBy, LocalDateTime createdOn, LocalDateTime updatedOn) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.symbol = symbol;
        this.currencyType = currencyType;
        this.status = status;
        this.decimalPlaces = decimalPlaces;
        this.minorUnitName = minorUnitName;
        this.isBaseCurrency = isBaseCurrency;
        this.walletEnabled = walletEnabled;
        this.depositEnabled = depositEnabled;
        this.withdrawalEnabled = withdrawalEnabled;
        this.fxEnabled = fxEnabled;
        this.minimumBalance = minimumBalance;
        this.countryCode = countryCode;
        this.updatedBy = updatedBy;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public CurrencyType getCurrencyType() { return currencyType; }
    public void setCurrencyType(CurrencyType currencyType) { this.currencyType = currencyType; }

    public CurrencyStatus getStatus() { return status; }
    public void setStatus(CurrencyStatus status) { this.status = status; }

    public int getDecimalPlaces() { return decimalPlaces; }
    public void setDecimalPlaces(int decimalPlaces) { this.decimalPlaces = decimalPlaces; }

    public String getMinorUnitName() { return minorUnitName; }
    public void setMinorUnitName(String minorUnitName) { this.minorUnitName = minorUnitName; }

    public boolean isBaseCurrency() { return isBaseCurrency; }
    public void setBaseCurrency(boolean baseCurrency) { isBaseCurrency = baseCurrency; }

    public boolean isWalletEnabled() { return walletEnabled; }
    public void setWalletEnabled(boolean walletEnabled) { this.walletEnabled = walletEnabled; }

    public boolean isDepositEnabled() { return depositEnabled; }
    public void setDepositEnabled(boolean depositEnabled) { this.depositEnabled = depositEnabled; }

    public boolean isWithdrawalEnabled() { return withdrawalEnabled; }
    public void setWithdrawalEnabled(boolean withdrawalEnabled) { this.withdrawalEnabled = withdrawalEnabled; }

    public boolean isFxEnabled() { return fxEnabled; }
    public void setFxEnabled(boolean fxEnabled) { this.fxEnabled = fxEnabled; }

    public BigDecimal getMinimumBalance() { return minimumBalance; }
    public void setMinimumBalance(BigDecimal minimumBalance) { this.minimumBalance = minimumBalance; }

    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }

    public LocalDateTime getUpdatedOn() { return updatedOn; }
    public void setUpdatedOn(LocalDateTime updatedOn) { this.updatedOn = updatedOn; }
}