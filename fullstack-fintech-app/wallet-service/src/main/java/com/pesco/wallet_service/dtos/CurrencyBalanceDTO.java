package com.pesco.wallet_service.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public class CurrencyBalanceDTO {
    
    private String currencyCode;
    private String currencySymbol;
    private BigDecimal balance;
    
    // Default constructor
    public CurrencyBalanceDTO() {
    }
    
    // Full constructor
    public CurrencyBalanceDTO(String currencyCode, String currencySymbol, BigDecimal balance) {
        this.currencyCode = currencyCode;
        this.currencySymbol = currencySymbol;
        this.balance = balance;
    }
    
    // Getters and Setters
    @JsonProperty("currency_code")
    public String getCurrencyCode() {
        return currencyCode;
    }
    
    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }
    
    @JsonProperty("currency_symbol")
    public String getCurrencySymbol() {
        return currencySymbol;
    }
    
    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }
    
    @JsonProperty("balance")
    public BigDecimal getBalance() {
        return balance;
    }
    
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}