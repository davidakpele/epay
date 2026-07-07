package com.epay.domain.notification.input;

import java.math.BigDecimal;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public class SwapCurrencyPayload {
    @NotBlank(message = "Email Address is mandatory")
    @Email(message = "Invalid email format")
    private String email;

    @NotNull(message = "Amount is mandatory")
    @Positive(message = "Amount balance must be positive")
    private BigDecimal amount;

    @NotNull(message = "Previous Balance is mandatory")
    @PositiveOrZero(message = "Previous balance must be positive or zero") 
    private BigDecimal previousBalance;

    @NotNull(message = "Available Balance is mandatory")
    @Positive(message = "Available balance must be positive")
    private BigDecimal availableBalance;

    @NotBlank(message = "Account Holder number is mandatory")
    private String accountHolder;

    private String currencySymbol;
    
    private String currencyExchange;


    public SwapCurrencyPayload() {
    }
    public SwapCurrencyPayload(String email, BigDecimal amount, BigDecimal previousBalance, BigDecimal availableBalance, String accountHolder, String currencySymbol, String currencyExchange) {
        this.email = email;
        this.amount = amount;
        this.previousBalance = previousBalance;
        this.availableBalance = availableBalance;
        this.accountHolder = accountHolder;
        this.currencySymbol = currencySymbol;
        this.currencyExchange = currencyExchange;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getPreviousBalance() {
        return this.previousBalance;
    }

    public void setPreviousBalance(BigDecimal previousBalance) {
        this.previousBalance = previousBalance;
    }

    public BigDecimal getAvailableBalance() {
        return this.availableBalance;
    }

    public void setAvailableBalance(BigDecimal availableBalance) {
        this.availableBalance = availableBalance;
    }

    public String getAccountHolder() {
        return this.accountHolder;
    }

    public void setAccountHolder(String accountHolder) {
        this.accountHolder = accountHolder;
    }

    public String getCurrencySymbol() {
        return this.currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public String getCurrencyExchange() {
        return this.currencyExchange;
    }

    public void setCurrencyExchange(String currencyExchange) {
        this.currencyExchange = currencyExchange;
    }


}
