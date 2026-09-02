package com.epay.domain.wallet.input;


import java.math.BigDecimal;

public class WalletRefundRequest {
    private Long senderId;
    private String currencyCode;
    private BigDecimal amount;


    public WalletRefundRequest() {
    }

    public WalletRefundRequest(Long senderId, String currencyCode, BigDecimal amount) {
        this.senderId = senderId;
        this.currencyCode = currencyCode;
        this.amount = amount;
    }

    public Long getSenderId() {
        return this.senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getCurrencyCode() {
        return this.currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
}
