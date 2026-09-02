package com.epay.domain.notification.input;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class DepositWalletNotification {
    @NotBlank(message = "Recipient email is mandatory")
    @Email(message = "Invalid email format")
    private String recipientEmail;

    @NotBlank(message = "Recipient name is mandatory")
    private String recipientName;

    @NotNull(message = "Deposit amount is mandatory")
    @Positive(message = "Deposit amount must be positive")
    private BigDecimal depositAmount;

    @NotNull(message = "Amount is mandatory")
    @Positive(message = "Amount balance must be positive")
    private BigDecimal amount;

    @NotNull(message = "Previous Balance is mandatory")
    @PositiveOrZero(message = "Previous balance must be positive or zero") 
    private BigDecimal previousBalance;

    @NotNull(message = "Available Balance is mandatory")
    @Positive(message = "Available balance must be positive")
    private BigDecimal availableBalance;

    @NotBlank(message = "Terminal number is mandatory")
    private String terminalNumber;

    @NotBlank(message = "Account Holder number is mandatory")
    private String accountHolder;

    private String currencySymbol;

    public DepositWalletNotification() {
    }

    @JsonCreator
    public DepositWalletNotification(
            @JsonProperty("recipientEmail") String recipientEmail,
            @JsonProperty("recipientName") String recipientName,
            @JsonProperty("depositAmount") BigDecimal depositAmount,
            @JsonProperty("amount") BigDecimal amount,
            @JsonProperty("previousBalance") BigDecimal previousBalance,
            @JsonProperty("availableBalance") BigDecimal availableBalance,
            @JsonProperty("terminalNumber") String terminalNumber,
            @JsonProperty("accountHolder") String accountHolder,
            @JsonProperty("currencySymbol") String currencySymbol) {
        this.recipientEmail = recipientEmail;
        this.recipientName = recipientName;
        this.depositAmount = depositAmount;
        this.amount = amount;
        this.previousBalance = previousBalance;
        this.availableBalance=availableBalance;
        this.terminalNumber = terminalNumber;
        this.accountHolder = accountHolder;
        this.currencySymbol =currencySymbol;
    }
    

    public String getRecipientEmail() {
        return this.recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public String getRecipientName() {
        return this.recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public BigDecimal getDepositAmount() {
        return this.depositAmount;
    }

    public void setDepositAmount(BigDecimal depositAmount) {
        this.depositAmount = depositAmount;
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

    public String getTerminalNumber() {
        return this.terminalNumber;
    }

    public void setTerminalNumber(String terminalNumber) {
        this.terminalNumber = terminalNumber;
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

}
