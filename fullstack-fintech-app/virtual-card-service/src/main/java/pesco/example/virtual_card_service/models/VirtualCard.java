package pesco.example.virtual_card_service.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Builder;
import lombok.Data;
import pesco.example.virtual_card_service.enums.CardPlan;
import pesco.example.virtual_card_service.enums.CardStatus;
import pesco.example.virtual_card_service.enums.CardType;
import pesco.example.virtual_card_service.enums.LimitPeriod;

@Data
@Builder
@Entity
@Table(name = "virtual_cards")
public class VirtualCard {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "card_id", nullable = false)
    private String cardId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "card_number", unique = true, nullable = false, length = 16)
    private String cardNumber; // Should be encrypted
    
    @Column(name = "card_holder_name", nullable = false)
    private String cardHolderName;
    
    @Column(name = "expiration_month", nullable = false, length = 2)
    private String expirationMonth; // "01" to "12"
    
    @Column(name = "expiration_year", nullable = false, length = 4)
    private String expirationYear; // "2025"
    
    @Column(name = "cvv", nullable = false, length = 3)
    private String cvv; // Should be encrypted
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardStatus status; // ACTIVE, FROZEN, CANCELLED, EXPIRED
    
    @Enumerated(EnumType.STRING)
    @Column(name = "card_type")
    private CardType cardType; // MASTER, VISA

    @Enumerated(EnumType.STRING)
    @Column(name = "card_plan")
    private CardPlan cardPlan; // JSON or structured string for subscription info
    
    @Column(name = "currency", nullable = false, length = 3)
    private String currency; // ISO 4217: USD, EUR, GBP
    
    @Column(name = "balance", precision = 19, scale = 2)
    private BigDecimal balance;
    
    @Column(name = "spending_limit", precision = 19, scale = 2)
    private BigDecimal spendingLimit;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "limit_period")
    private LimitPeriod limitPeriod; // TRANSACTION, DAILY, WEEKLY, MONTHLY
    
    @Column(name = "limit_reset_date")
    private LocalDate limitResetDate;
    
    @Column(name = "current_period_spent", precision = 19, scale = 2)
    private BigDecimal currentPeriodSpent;
    
    // Merchant Information
    @Column(name = "merchant_name")
    private String merchantName;
    
    @Column(name = "merchant_id")
    private String merchantId;
    
    @Column(name = "merchant_category_code", length = 4)
    private String merchantCategoryCode; // MCC
    
    @Column(name = "merchant_country", length = 2)
    private String merchantCountry;
    
    @Column(name = "merchant_city")
    private String merchantCity;

    @Column(name = "authorization_code")
    private String authorizationCode;
    // Security & Controls
    @Column(name = "allow_international")
    private Boolean allowInternational;
    
    @Column(name = "allow_online")
    private Boolean allowOnline;
    
    @Column(name = "allow_atm")
    private Boolean allowAtm;
    
    @Column(name = "allow_contactless")
    private Boolean allowContactless;
    
    // Metadata
    @Column(name = "bin", length = 6)
    private String bin; // Bank Identification Number (first 6 digits)
    
    @Column(name = "first_four", length = 4)
    private String firstFour; // First 4 digits for display
    
    @Column(name = "last_four", length = 4)
    private String lastFour; // Last 4 digits for display
    
    @Column(name = "masked_card_number", length = 19)
    private String maskedCardNumber; // "****-****-****-1234"
    
    @Column(name = "hashed_card_number")
    private String hashedCardNumber; // Hashed complete card number for security
    
    @Column(name = "provider_card_id")
    private String providerCardId; // External card provider reference ID
    
    // Timestamps
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "activated_at")
    private LocalDateTime activatedAt;
    
    @Column(name = "frozen_at")
    private LocalDateTime frozenAt;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;
    
    // Soft delete
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    @Version
    private Long version; // Optimistic locking
    
    // Helper methods
    @PrePersist
    protected void onCreate() {
        if (this.balance == null) {
            this.balance = BigDecimal.ZERO;
        }
        if (this.currentPeriodSpent == null) {
            this.currentPeriodSpent = BigDecimal.ZERO;
        }
        if (this.allowInternational == null) {
            this.allowInternational = true;
        }
        if (this.allowOnline == null) {
            this.allowOnline = true;
        }
        if (this.allowAtm == null) {
            this.allowAtm = false;
        }
        if (this.allowContactless == null) {
            this.allowContactless = true;
        }
    }
    
    public boolean isActive() {
        return this.status == CardStatus.ACTIVE 
            && this.expiresAt.isAfter(LocalDateTime.now())
            && this.deletedAt == null;
    }
    
    public boolean isExpired() {
        return this.expiresAt.isBefore(LocalDateTime.now());
    }
    
    public boolean hasAvailableBalance(BigDecimal amount) {
        return this.balance.compareTo(amount) >= 0;
    }
    
    public boolean isWithinSpendingLimit(BigDecimal amount) {
        if (this.spendingLimit == null) {
            return true;
        }
        BigDecimal totalSpent = this.currentPeriodSpent.add(amount);
        return totalSpent.compareTo(this.spendingLimit) <= 0;
    }


    public VirtualCard() {
    }

    public VirtualCard(String id, String cardId, Long userId, String cardNumber, String cardHolderName, String expirationMonth, String expirationYear, String cvv, CardStatus status, CardType cardType, CardPlan cardPlan, String currency, BigDecimal balance, BigDecimal spendingLimit, LimitPeriod limitPeriod, LocalDate limitResetDate, BigDecimal currentPeriodSpent, String merchantName, String merchantId, String merchantCategoryCode, String merchantCountry, String merchantCity, String authorizationCode, Boolean allowInternational, Boolean allowOnline, Boolean allowAtm, Boolean allowContactless, String bin, String firstFour, String lastFour, String maskedCardNumber, String hashedCardNumber, String providerCardId, LocalDateTime expiresAt, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime activatedAt, LocalDateTime frozenAt, LocalDateTime cancelledAt, LocalDateTime lastUsedAt, LocalDateTime deletedAt, Long version) {
        this.id = id;
        this.cardId = cardId;
        this.userId = userId;
        this.cardNumber = cardNumber;
        this.cardHolderName = cardHolderName;
        this.expirationMonth = expirationMonth;
        this.expirationYear = expirationYear;
        this.cvv = cvv;
        this.status = status;
        this.cardType = cardType;
        this.cardPlan = cardPlan;
        this.currency = currency;
        this.balance = balance;
        this.spendingLimit = spendingLimit;
        this.limitPeriod = limitPeriod;
        this.limitResetDate = limitResetDate;
        this.currentPeriodSpent = currentPeriodSpent;
        this.merchantName = merchantName;
        this.merchantId = merchantId;
        this.merchantCategoryCode = merchantCategoryCode;
        this.merchantCountry = merchantCountry;
        this.merchantCity = merchantCity;
        this.authorizationCode = authorizationCode;
        this.allowInternational = allowInternational;
        this.allowOnline = allowOnline;
        this.allowAtm = allowAtm;
        this.allowContactless = allowContactless;
        this.bin = bin;
        this.firstFour = firstFour;
        this.lastFour = lastFour;
        this.maskedCardNumber = maskedCardNumber;
        this.hashedCardNumber = hashedCardNumber;
        this.providerCardId = providerCardId;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.activatedAt = activatedAt;
        this.frozenAt = frozenAt;
        this.cancelledAt = cancelledAt;
        this.lastUsedAt = lastUsedAt;
        this.deletedAt = deletedAt;
        this.version = version;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCardId() {
        return this.cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCardNumber() {
        return this.cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardHolderName() {
        return this.cardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    public String getExpirationMonth() {
        return this.expirationMonth;
    }

    public void setExpirationMonth(String expirationMonth) {
        this.expirationMonth = expirationMonth;
    }

    public String getExpirationYear() {
        return this.expirationYear;
    }

    public void setExpirationYear(String expirationYear) {
        this.expirationYear = expirationYear;
    }

    public String getCvv() {
        return this.cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public CardStatus getStatus() {
        return this.status;
    }

    public void setStatus(CardStatus status) {
        this.status = status;
    }

    public CardType getCardType() {
        return this.cardType;
    }

    public void setCardType(CardType cardType) {
        this.cardType = cardType;
    }

    public CardPlan getCardPlan() {
        return this.cardPlan;
    }

    public void setCardPlan(CardPlan cardPlan) {
        this.cardPlan = cardPlan;
    }

    public String getCurrency() {
        return this.currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getBalance() {
        return this.balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getSpendingLimit() {
        return this.spendingLimit;
    }

    public void setSpendingLimit(BigDecimal spendingLimit) {
        this.spendingLimit = spendingLimit;
    }

    public LimitPeriod getLimitPeriod() {
        return this.limitPeriod;
    }

    public void setLimitPeriod(LimitPeriod limitPeriod) {
        this.limitPeriod = limitPeriod;
    }

    public LocalDate getLimitResetDate() {
        return this.limitResetDate;
    }

    public void setLimitResetDate(LocalDate limitResetDate) {
        this.limitResetDate = limitResetDate;
    }

    public BigDecimal getCurrentPeriodSpent() {
        return this.currentPeriodSpent;
    }

    public void setCurrentPeriodSpent(BigDecimal currentPeriodSpent) {
        this.currentPeriodSpent = currentPeriodSpent;
    }

    public String getMerchantName() {
        return this.merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getMerchantId() {
        return this.merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getMerchantCategoryCode() {
        return this.merchantCategoryCode;
    }

    public void setMerchantCategoryCode(String merchantCategoryCode) {
        this.merchantCategoryCode = merchantCategoryCode;
    }

    public String getMerchantCountry() {
        return this.merchantCountry;
    }

    public void setMerchantCountry(String merchantCountry) {
        this.merchantCountry = merchantCountry;
    }

    public String getMerchantCity() {
        return this.merchantCity;
    }

    public void setMerchantCity(String merchantCity) {
        this.merchantCity = merchantCity;
    }

    public String getAuthorizationCode() {
        return this.authorizationCode;
    }

    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }

    public Boolean isAllowInternational() {
        return this.allowInternational;
    }

    public Boolean getAllowInternational() {
        return this.allowInternational;
    }

    public void setAllowInternational(Boolean allowInternational) {
        this.allowInternational = allowInternational;
    }

    public Boolean isAllowOnline() {
        return this.allowOnline;
    }

    public Boolean getAllowOnline() {
        return this.allowOnline;
    }

    public void setAllowOnline(Boolean allowOnline) {
        this.allowOnline = allowOnline;
    }

    public Boolean isAllowAtm() {
        return this.allowAtm;
    }

    public Boolean getAllowAtm() {
        return this.allowAtm;
    }

    public void setAllowAtm(Boolean allowAtm) {
        this.allowAtm = allowAtm;
    }

    public Boolean isAllowContactless() {
        return this.allowContactless;
    }

    public Boolean getAllowContactless() {
        return this.allowContactless;
    }

    public void setAllowContactless(Boolean allowContactless) {
        this.allowContactless = allowContactless;
    }

    public String getBin() {
        return this.bin;
    }

    public void setBin(String bin) {
        this.bin = bin;
    }

    public String getFirstFour() {
        return this.firstFour;
    }

    public void setFirstFour(String firstFour) {
        this.firstFour = firstFour;
    }

    public String getLastFour() {
        return this.lastFour;
    }

    public void setLastFour(String lastFour) {
        this.lastFour = lastFour;
    }

    public String getMaskedCardNumber() {
        return this.maskedCardNumber;
    }

    public void setMaskedCardNumber(String maskedCardNumber) {
        this.maskedCardNumber = maskedCardNumber;
    }

    public String getHashedCardNumber() {
        return this.hashedCardNumber;
    }

    public void setHashedCardNumber(String hashedCardNumber) {
        this.hashedCardNumber = hashedCardNumber;
    }

    public String getProviderCardId() {
        return this.providerCardId;
    }

    public void setProviderCardId(String providerCardId) {
        this.providerCardId = providerCardId;
    }

    public LocalDateTime getExpiresAt() {
        return this.expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getActivatedAt() {
        return this.activatedAt;
    }

    public void setActivatedAt(LocalDateTime activatedAt) {
        this.activatedAt = activatedAt;
    }

    public LocalDateTime getFrozenAt() {
        return this.frozenAt;
    }

    public void setFrozenAt(LocalDateTime frozenAt) {
        this.frozenAt = frozenAt;
    }

    public LocalDateTime getCancelledAt() {
        return this.cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public LocalDateTime getLastUsedAt() {
        return this.lastUsedAt;
    }

    public void setLastUsedAt(LocalDateTime lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }

    public LocalDateTime getDeletedAt() {
        return this.deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public Long getVersion() {
        return this.version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

}