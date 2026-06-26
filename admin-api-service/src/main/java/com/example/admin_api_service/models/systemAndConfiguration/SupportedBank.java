package com.example.admin_api_service.models.systemAndConfiguration;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.admin_api_service.enums.BankStatus;
import com.example.admin_api_service.enums.BankType;

@Entity
@Table(name = "supported_banks")
public class SupportedBank {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id = UUID.randomUUID().toString();

    // CBN-assigned bank code e.g. "058" for GTBank
    @Column(name = "bank_code", length = 20, nullable = false, unique = true)
    private String bankCode;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "short_name", length = 50)
    private String shortName;

    @Enumerated(EnumType.STRING)
    @Column(name = "bank_type", length = 30, nullable = false)
    private BankType bankType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private BankStatus status = BankStatus.ACTIVE;

    // Country this bank operates in (ISO 3166-1 alpha-2)
    @Column(name = "country_code", length = 5, nullable = false)
    private String countryCode;

    // NIBSS institution code for NIP transactions
    @Column(name = "nibss_bank_code", length = 20)
    private String nibssBankCode;

    // SWIFT/BIC code for international transfers
    @Column(name = "swift_code", length = 20)
    private String swiftCode;

    // Routing/sort code for interbank transfers
    @Column(name = "routing_number", length = 20)
    private String routingNumber;

    // URL of bank logo image
    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    // Whether NIP (NIBSS Instant Payment) is supported
    @Column(name = "nip_enabled", nullable = false)
    private boolean nipEnabled = true;

    // Whether name enquiry (account lookup) is supported
    @Column(name = "name_enquiry_enabled", nullable = false)
    private boolean nameEnquiryEnabled = true;

    // Whether transfers to this bank are currently allowed
    @Column(name = "transfer_enabled", nullable = false)
    private boolean transferEnabled = true;

    // Whether direct debit mandates are supported
    @Column(name = "direct_debit_enabled", nullable = false)
    private boolean directDebitEnabled = false;

    // Average transfer success rate (0.00–100.00) — updated periodically
    @Column(name = "success_rate", precision = 5, scale = 2)
    private java.math.BigDecimal successRate;

    // Average response time in milliseconds — updated periodically
    @Column(name = "avg_response_time_ms")
    private Long avgResponseTimeMs;

    // Reason transfers are currently disabled (if any)
    @Column(name = "disabled_reason", length = 500)
    private String disabledReason;

    @Column(name = "updated_by", length = 36)
    private String updatedBy;

    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn = LocalDateTime.now();

    @Column(name = "updated_on", nullable = false)
    private LocalDateTime updatedOn = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() { this.updatedOn = LocalDateTime.now(); }

    public SupportedBank() {
    }

    public SupportedBank(String id, String bankCode, String name, String shortName, BankType bankType, BankStatus status, String countryCode, String nibssBankCode, String swiftCode, String routingNumber, String logoUrl, boolean nipEnabled, boolean nameEnquiryEnabled, boolean transferEnabled, boolean directDebitEnabled, java.math.BigDecimal successRate, Long avgResponseTimeMs, String disabledReason, String updatedBy, LocalDateTime createdOn, LocalDateTime updatedOn) {
        this.id = id;
        this.bankCode = bankCode;
        this.name = name;
        this.shortName = shortName;
        this.bankType = bankType;
        this.status = status;
        this.countryCode = countryCode;
        this.nibssBankCode = nibssBankCode;
        this.swiftCode = swiftCode;
        this.routingNumber = routingNumber;
        this.logoUrl = logoUrl;
        this.nipEnabled = nipEnabled;
        this.nameEnquiryEnabled = nameEnquiryEnabled;
        this.transferEnabled = transferEnabled;
        this.directDebitEnabled = directDebitEnabled;
        this.successRate = successRate;
        this.avgResponseTimeMs = avgResponseTimeMs;
        this.disabledReason = disabledReason;
        this.updatedBy = updatedBy;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBankCode() { return bankCode; }
    public void setBankCode(String bankCode) { this.bankCode = bankCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getShortName() { return shortName; }
    public void setShortName(String shortName) { this.shortName = shortName; }

    public BankType getBankType() { return bankType; }
    public void setBankType(BankType bankType) { this.bankType = bankType; }

    public BankStatus getStatus() { return status; }
    public void setStatus(BankStatus status) { this.status = status; }

    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

    public String getNibssBankCode() { return nibssBankCode; }
    public void setNibssBankCode(String nibssBankCode) { this.nibssBankCode = nibssBankCode; }

    public String getSwiftCode() { return swiftCode; }
    public void setSwiftCode(String swiftCode) { this.swiftCode = swiftCode; }

    public String getRoutingNumber() { return routingNumber; }
    public void setRoutingNumber(String routingNumber) { this.routingNumber = routingNumber; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public boolean isNipEnabled() { return nipEnabled; }
    public void setNipEnabled(boolean nipEnabled) { this.nipEnabled = nipEnabled; }

    public boolean isNameEnquiryEnabled() { return nameEnquiryEnabled; }
    public void setNameEnquiryEnabled(boolean nameEnquiryEnabled) { this.nameEnquiryEnabled = nameEnquiryEnabled; }

    public boolean isTransferEnabled() { return transferEnabled; }
    public void setTransferEnabled(boolean transferEnabled) { this.transferEnabled = transferEnabled; }

    public boolean isDirectDebitEnabled() { return directDebitEnabled; }
    public void setDirectDebitEnabled(boolean directDebitEnabled) { this.directDebitEnabled = directDebitEnabled; }

    public java.math.BigDecimal getSuccessRate() { return successRate; }
    public void setSuccessRate(java.math.BigDecimal successRate) { this.successRate = successRate; }

    public Long getAvgResponseTimeMs() { return avgResponseTimeMs; }
    public void setAvgResponseTimeMs(Long avgResponseTimeMs) { this.avgResponseTimeMs = avgResponseTimeMs; }

    public String getDisabledReason() { return disabledReason; }
    public void setDisabledReason(String disabledReason) { this.disabledReason = disabledReason; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }

    public LocalDateTime getUpdatedOn() { return updatedOn; }
    public void setUpdatedOn(LocalDateTime updatedOn) { this.updatedOn = updatedOn; }
}
