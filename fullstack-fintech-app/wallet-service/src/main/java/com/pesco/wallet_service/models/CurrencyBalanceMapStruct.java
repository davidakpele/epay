package com.pesco.wallet_service.models;


import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
@Embeddable
public class CurrencyBalanceMapStruct {

    @Column(name = "currency_code", nullable = false)
    private String currencyCode;

    @Column(name = "currency_symbol", nullable = false)
    private String currencySymbol;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    public CurrencyBalanceMapStruct() {
    }

    public CurrencyBalanceMapStruct(String currencyCode, String currencySymbol, BigDecimal balance) {
        this.currencyCode = currencyCode;
        this.currencySymbol = currencySymbol;
        this.balance = balance;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
