package com.epay.domain.withdraw.input;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import com.epay.domain.withdraw.enums.WithdrawalType;

@Data
@Builder
public class BankWithdrawRequest {

    @NotBlank(message = "Username is required")
    private String username;
    
    @NotBlank(message = "Account number is required")
    @Pattern(regexp = "\\d{10,16}", message = "Account number must be 10-16 digits")
    private String accountNumber;
    
    @NotBlank(message = "Bank code is required")
    private String bankCode;
    
    @NotBlank(message = "Account name is required")
    @Size(min = 2, max = 100, message = "Account name must be between 2 and 100 characters")
    private String accountName;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    @NotBlank(message = "Currency is required")
    private String currency;

    @NotBlank(message = "Wallet Id is required")
    private Long walletId;

    @NotBlank(message = "Transfer Pin is required")
    @Size(min = 4, max = 4, message = "Your Transfer Pin must be 4 Digit")
    @NotNull(message = "Transfer Pin is required")
    private String transferPin;

    @NotBlank(message = "User Id is required")
    @NotNull(message = "User Id is required")
    private Long userId;

    @NotBlank(message = "Idempotency key is required")
    @NotNull(message = "Idempotency key is required")
    private String idempotencyKey;
    
    @NotNull(message = "Withdrawal type is required")
    @NotBlank(message = "Withdrawal type is required")
    private WithdrawalType withdrawalType;

    private String narration;

    public BankWithdrawRequest() {
    }

    public BankWithdrawRequest(String username, String accountNumber, String bankCode, String accountName, BigDecimal amount, String currency, Long walletId, String transferPin, Long userId, String idempotencyKey, WithdrawalType withdrawalType, String narration) {
        this.username = username;
        this.accountNumber = accountNumber;
        this.bankCode = bankCode;
        this.accountName = accountName;
        this.amount = amount;
        this.currency = currency;
        this.walletId = walletId;
        this.transferPin = transferPin;
        this.userId = userId;
        this.idempotencyKey = idempotencyKey;
        this.withdrawalType = withdrawalType;
        this.narration = narration;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAccountNumber() {
        return this.accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getBankCode() {
        return this.bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getAccountName() {
        return this.accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return this.currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Long getWalletId() {
        return this.walletId;
    }

    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }

    public String getTransferPin() {
        return this.transferPin;
    }

    public void setTransferPin(String transferPin) {
        this.transferPin = transferPin;
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getIdempotencyKey() {
        return this.idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public WithdrawalType getWithdrawalType() {
        return this.withdrawalType;
    }

    public void setWithdrawalType(WithdrawalType withdrawalType) {
        this.withdrawalType = withdrawalType;
    }

    public String getNarration() {
        return this.narration;
    }

    public void setNarration(String narration) {
        this.narration = narration;
    }

}