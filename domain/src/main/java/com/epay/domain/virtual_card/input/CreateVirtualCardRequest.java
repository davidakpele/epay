package com.epay.domain.virtual_card.input;

import java.math.BigDecimal;

import com.epay.domain.virtual_card.enums.CardPlan;
import com.epay.domain.virtual_card.enums.CardType;
import com.epay.domain.virtual_card.enums.LimitPeriod;

public class CreateVirtualCardRequest {
    private Long userId;
    private String accountHolderName;
    private CardType cardType;
    private String currency;
    private BigDecimal initialBalance;
    private BigDecimal spendingLimit;
    private LimitPeriod limitPeriod;
    private CardPlan plan;
    private Boolean allowInternational;
    private Boolean allowOnline;
    private Boolean allowAtm;
    private Boolean allowContactless;
    private String merchantName;
    private String merchantId;
    private String merchantCategoryCode;
    private String merchantCountry;
    private String merchantCity;

    public CreateVirtualCardRequest() {
    }

    public CreateVirtualCardRequest(Long userId, String accountHolderName, CardType cardType, String currency, BigDecimal initialBalance, BigDecimal spendingLimit, LimitPeriod limitPeriod, CardPlan plan, Boolean allowInternational, Boolean allowOnline, Boolean allowAtm, Boolean allowContactless, String merchantName, String merchantId, String merchantCategoryCode, String merchantCountry, String merchantCity) {
        this.userId = userId;
        this.accountHolderName = accountHolderName;
        this.cardType = cardType;
        this.currency = currency;
        this.initialBalance = initialBalance;
        this.spendingLimit = spendingLimit;
        this.limitPeriod = limitPeriod;
        this.plan = plan;
        this.allowInternational = allowInternational;
        this.allowOnline = allowOnline;
        this.allowAtm = allowAtm;
        this.allowContactless = allowContactless;
        this.merchantName = merchantName;
        this.merchantId = merchantId;
        this.merchantCategoryCode = merchantCategoryCode;
        this.merchantCountry = merchantCountry;
        this.merchantCity = merchantCity;
    }


    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getAccountHolderName() {
        return this.accountHolderName;
    }

    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }

    public CardType getCardType() {
        return this.cardType;
    }

    public void setCardType(CardType cardType) {
        this.cardType = cardType;
    }

    public String getCurrency() {
        return this.currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getInitialBalance() {
        return this.initialBalance;
    }

    public void setInitialBalance(BigDecimal initialBalance) {
        this.initialBalance = initialBalance;
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

    public CardPlan getPlan() {
        return this.plan;
    }

    public void setPlan(CardPlan plan) {
        this.plan = plan;
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

}
