package com.pesco.wallet_service.payloads;

import java.math.BigDecimal;

import com.pesco.wallet_service.enums.Currency;

public class DeductAmountRequest {
    private Long id;
    private Currency currencyType;
    private BigDecimal amount;

    public DeductAmountRequest(Long id, Currency currencyType, BigDecimal amount) {
        this.id = id;
        this.currencyType = currencyType;
        this.amount = amount;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
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
