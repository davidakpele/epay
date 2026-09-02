package com.epay.virtual_card.responses;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.epay.domain.virtual_card.entity.CardLimit;
import com.epay.domain.virtual_card.enums.CardPlan;
import com.epay.domain.virtual_card.enums.CardStatus;
import com.epay.domain.virtual_card.enums.CardType;
import com.epay.domain.virtual_card.enums.LimitPeriod;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VirtualCardResponse {
    private String id;
    private String cardId;
    private Long userId;
    private String cardHolderName;
    private String lastFour; 
    private String firstFour; 
    private String hashedCardNumber;
    private String expirationMonth; 
    private String expirationYear;
    private CardStatus status;
    private CardType cardType;
    private CardPlan cardPlan;
    private String currency; 
    private BigDecimal balance;
    private BigDecimal spendingLimit;
    private LimitPeriod limitPeriod; 
    private BigDecimal currentPeriodSpent;
    private Boolean allowInternational;
    private Boolean allowOnline;
    private Boolean allowAtm;
    private Boolean allowContactless;
    private String merchantName;
    private String merchantId;
    private String merchantCategoryCode; 
    private String merchantCountry;
    private String merchantCity;
    private String maskedCardNumber;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime lastUsedAt;
    private CardLimit cardLimit;

    public VirtualCardResponse() {
    }


    public VirtualCardResponse(String id, String cardId, Long userId, String cardHolderName, String lastFour, String firstFour, String hashedCardNumber, String expirationMonth, String expirationYear, CardStatus status, CardType cardType, CardPlan cardPlan, String currency, BigDecimal balance, BigDecimal spendingLimit, LimitPeriod limitPeriod, BigDecimal currentPeriodSpent, Boolean allowInternational, Boolean allowOnline, Boolean allowAtm, Boolean allowContactless, String merchantName, String merchantId, String merchantCategoryCode, String merchantCountry, String merchantCity, String maskedCardNumber, LocalDateTime expiresAt, LocalDateTime createdAt, LocalDateTime lastUsedAt, CardLimit cardLimit) {
        this.id = id;
        this.cardId = cardId;
        this.userId = userId;
        this.cardHolderName = cardHolderName;
        this.lastFour = lastFour;
        this.firstFour = firstFour;
        this.hashedCardNumber = hashedCardNumber;
        this.expirationMonth = expirationMonth;
        this.expirationYear = expirationYear;
        this.status = status;
        this.cardType = cardType;
        this.cardPlan = cardPlan;
        this.currency = currency;
        this.balance = balance;
        this.spendingLimit = spendingLimit;
        this.limitPeriod = limitPeriod;
        this.currentPeriodSpent = currentPeriodSpent;
        this.allowInternational = allowInternational;
        this.allowOnline = allowOnline;
        this.allowAtm = allowAtm;
        this.allowContactless = allowContactless;
        this.merchantName = merchantName;
        this.merchantId = merchantId;
        this.merchantCategoryCode = merchantCategoryCode;
        this.merchantCountry = merchantCountry;
        this.merchantCity = merchantCity;
        this.maskedCardNumber = maskedCardNumber;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.lastUsedAt = lastUsedAt;
        this.cardLimit = cardLimit;
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

    public String getCardHolderName() {
        return this.cardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    public String getLastFour() {
        return this.lastFour;
    }

    public void setLastFour(String lastFour) {
        this.lastFour = lastFour;
    }

    public String getFirstFour() {
        return this.firstFour;
    }

    public void setFirstFour(String firstFour) {
        this.firstFour = firstFour;
    }

    public String getHashedCardNumber() {
        return this.hashedCardNumber;
    }

    public void setHashedCardNumber(String hashedCardNumber) {
        this.hashedCardNumber = hashedCardNumber;
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

    public BigDecimal getCurrentPeriodSpent() {
        return this.currentPeriodSpent;
    }

    public void setCurrentPeriodSpent(BigDecimal currentPeriodSpent) {
        this.currentPeriodSpent = currentPeriodSpent;
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

    public String getMaskedCardNumber() {
        return this.maskedCardNumber;
    }

    public void setMaskedCardNumber(String maskedCardNumber) {
        this.maskedCardNumber = maskedCardNumber;
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

    public LocalDateTime getLastUsedAt() {
        return this.lastUsedAt;
    }

    public void setLastUsedAt(LocalDateTime lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }

    public CardLimit getCardLimit() {
        return this.cardLimit;
    }

    public void setCardLimit(CardLimit cardLimit) {
        this.cardLimit = cardLimit;
    }


}
