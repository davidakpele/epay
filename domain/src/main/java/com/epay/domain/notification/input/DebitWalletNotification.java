package com.epay.domain.notification.input;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DebitWalletNotification {
    @NotBlank(message = "Sender email is mandatory")
    @Email(message = "Sender email should be valid")
    private String senderEmail;

    @NotNull(message = "Fee amount is mandatory")
    private BigDecimal feeAmount;

    @NotNull(message = "Transfer amount is mandatory")
    private BigDecimal transferAmount;

    @NotBlank(message = "Sender full name is mandatory")
    private String senderFullName;

    @NotBlank(message = "Receiver full name is mandatory")
    private String receiverFullName;

    @NotNull(message = "Balance is mandatory")
    private BigDecimal balance;

    @NotBlank(message = "Currency is mandatory")
    private String currency;

    @NotBlank(message = "Transaction Id is mandatory")
    private String transactionId;

    @NotNull(message = "Previous balance is required")
    private BigDecimal previousBalance;

    public DebitWalletNotification() {
    }

    @JsonCreator
    public DebitWalletNotification(
            @JsonProperty("senderEmail") String senderEmail,
            @JsonProperty("feeAmount") BigDecimal feeAmount,
            @JsonProperty("transferAmount") BigDecimal transferAmount,
            @JsonProperty("senderFullName") String senderFullName,
            @JsonProperty("receiverFullName") String receiverFullName,
            @JsonProperty("balance") BigDecimal balance,
            @JsonProperty("currency") String currency,
            @JsonProperty("transactionId") String transactionId,
            @JsonProperty("previousBalance") BigDecimal previousBalance) {
        this.senderEmail = senderEmail;
        this.feeAmount = feeAmount;
        this.transferAmount = transferAmount;
        this.senderFullName = senderFullName;
        this.receiverFullName = receiverFullName;
        this.balance = balance;
        this.currency = currency;
        this.transactionId = transactionId;
        this.previousBalance = previousBalance;
    }


    public String getSenderEmail() {
        return this.senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public BigDecimal getFeeAmount() {
        return this.feeAmount;
    }

    public void setFeeAmount(BigDecimal feeAmount) {
        this.feeAmount = feeAmount;
    }

    public BigDecimal getTransferAmount() {
        return this.transferAmount;
    }

    public void setTransferAmount(BigDecimal transferAmount) {
        this.transferAmount = transferAmount;
    }

    public String getSenderFullName() {
        return this.senderFullName;
    }

    public void setSenderFullName(String senderFullName) {
        this.senderFullName = senderFullName;
    }

    public String getReceiverFullName() {
        return this.receiverFullName;
    }

    public void setReceiverFullName(String receiverFullName) {
        this.receiverFullName = receiverFullName;
    }

    public BigDecimal getBalance() {
        return this.balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return this.currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getTransactionId() {
        return this.transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
   
    public BigDecimal getPreviousBalance() {
        return this.previousBalance;
    }

    public void setPreviousBalance(BigDecimal previousBalance) {
        this.previousBalance = previousBalance;
    }

}
