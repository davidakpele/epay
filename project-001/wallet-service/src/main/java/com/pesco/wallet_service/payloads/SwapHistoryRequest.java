package com.pesco.wallet_service.payloads;

import java.math.BigDecimal;
import lombok.Data;

@Data  
public class SwapHistoryRequest {
    private BigDecimal amount;
    private BigDecimal previousBalance;
    private BigDecimal available;
    private String description;
    private String userFullName;
    private Long userId;
    private String currencyType;
    private String transactionType;
    private Long walletId;


    public SwapHistoryRequest() {
    }


    public SwapHistoryRequest(BigDecimal amount, BigDecimal previousBalance, BigDecimal available, String description, String userFullName, Long userId, String currencyType, String transactionType, Long walletId) {
        this.amount = amount;
        this.previousBalance = previousBalance;
        this.available = available;
        this.description = description;
        this.userFullName = userFullName;
        this.userId = userId;
        this.currencyType = currencyType;
        this.transactionType = transactionType;
        this.walletId = walletId;
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

    public BigDecimal getAvailable() {
        return this.available;
    }

    public void setAvailable(BigDecimal available) {
        this.available = available;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUserFullName() {
        return this.userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCurrencyType() {
        return this.currencyType;
    }

    public void setCurrencyType(String currencyType) {
        this.currencyType = currencyType;
    }

    public String getTransactionType() {
        return this.transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public Long getWalletId() {
        return this.walletId;
    }

    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }



}