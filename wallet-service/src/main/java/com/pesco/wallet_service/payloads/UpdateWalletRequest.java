package com.pesco.wallet_service.payloads;

import java.math.BigDecimal;

import com.pesco.wallet_service.enums.Currency;

public class UpdateWalletRequest {
    private Long userId;
    private Currency currencyType;
    private BigDecimal amount;

    public UpdateWalletRequest(Long userId, BigDecimal amount, Currency currencyType) {
        this.userId = userId;
        this.amount = amount;
        this.currencyType = currencyType;
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Currency getCurrencyType() {
        return this.currencyType;
    }

    public void setCurrencyType(Currency currencyType) {
        this.currencyType = currencyType;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

}
