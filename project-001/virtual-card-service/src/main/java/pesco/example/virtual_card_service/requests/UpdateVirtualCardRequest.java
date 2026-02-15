package pesco.example.virtual_card_service.requests;

import java.math.BigDecimal;
import pesco.example.virtual_card_service.enums.CardStatus;
import pesco.example.virtual_card_service.enums.LimitPeriod;

public class UpdateVirtualCardRequest {
    private Long cardId;
    private String accountHolderName;
    private BigDecimal spendingLimit;
    private LimitPeriod limitPeriod;
    private CardStatus status;
    private Boolean allowInternational;
    private Boolean allowOnline;
    private Boolean allowAtm;
    private Boolean allowContactless;
    private String merchantName;
    private String merchantId;
    private String merchantCategoryCode;
    private String merchantCountry;
    private String merchantCity;

    public UpdateVirtualCardRequest() {
    }

    public UpdateVirtualCardRequest(Long cardId, String accountHolderName, BigDecimal spendingLimit, LimitPeriod limitPeriod, CardStatus status, Boolean allowInternational, Boolean allowOnline, Boolean allowAtm, Boolean allowContactless, String merchantName, String merchantId, String merchantCategoryCode, String merchantCountry, String merchantCity) {
        this.cardId = cardId;
        this.accountHolderName = accountHolderName;
        this.spendingLimit = spendingLimit;
        this.limitPeriod = limitPeriod;
        this.status = status;
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

    public Long getCardId() {
        return this.cardId;
    }

    public void setCardId(Long cardId) {
        this.cardId = cardId;
    }

    public String getAccountHolderName() {
        return this.accountHolderName;
    }

    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
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

    public CardStatus getStatus() {
        return this.status;
    }

    public void setStatus(CardStatus status) {
        this.status = status;
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
