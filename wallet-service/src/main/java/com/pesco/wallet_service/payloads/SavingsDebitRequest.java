package com.pesco.wallet_service.payloads;

import java.math.BigDecimal;

public class SavingsDebitRequest {
    private Long userId;
    private Long walletId;
    private String currencyType;
    private BigDecimal amount;
    private String description;
    private String referenceNo;

    public SavingsDebitRequest() {}

    public SavingsDebitRequest(Long userId, Long walletId, String currencyType,
                                BigDecimal amount, String description, String referenceNo) {
        this.userId = userId;
        this.walletId = walletId;
        this.currencyType = currencyType;
        this.amount = amount;
        this.description = description;
        this.referenceNo = referenceNo;
    }

    public Long getUserId()                      { return userId; }
    public void setUserId(Long userId)           { this.userId = userId; }
    public Long getWalletId()                    { return walletId; }
    public void setWalletId(Long walletId)       { this.walletId = walletId; }
    public String getCurrencyType()              { return currencyType; }
    public void setCurrencyType(String c)        { this.currencyType = c; }
    public BigDecimal getAmount()                { return amount; }
    public void setAmount(BigDecimal amount)     { this.amount = amount; }
    public String getDescription()               { return description; }
    public void setDescription(String d)         { this.description = d; }
    public String getReferenceNo()               { return referenceNo; }
    public void setReferenceNo(String r)         { this.referenceNo = r; }
}
