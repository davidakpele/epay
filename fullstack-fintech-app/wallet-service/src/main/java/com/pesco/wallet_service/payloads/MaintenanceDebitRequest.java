package com.pesco.wallet_service.payloads;

import java.math.BigDecimal;

public class MaintenanceDebitRequest {
    private Long userId;
    private Long walletId;
    private String currencyType;
    private BigDecimal amount;
    private String description;
    private String referenceNo;

    // Default constructor
    public MaintenanceDebitRequest() {
    }

    // Constructor with parameters
    public MaintenanceDebitRequest(Long userId, Long walletId, String currencyType, 
                                  BigDecimal amount, String description, String referenceNo) {
        this.userId = userId;
        this.walletId = walletId;
        this.currencyType = currencyType;
        this.amount = amount;
        this.description = description;
        this.referenceNo = referenceNo;
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getWalletId() {
        return walletId;
    }

    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }

    public String getCurrencyType() {
        return currencyType;
    }

    public void setCurrencyType(String currencyType) {
        this.currencyType = currencyType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReferenceNo() {
        return referenceNo;
    }

    public void setReferenceNo(String referenceNo) {
        this.referenceNo = referenceNo;
    }

    // toString method
    @Override
    public String toString() {
        return "MaintenanceDebitRequest{" +
                "userId=" + userId +
                ", walletId=" + walletId +
                ", currencyType='" + currencyType + '\'' +
                ", amount=" + amount +
                ", description='" + description + '\'' +
                ", referenceNo='" + referenceNo + '\'' +
                '}';
    }
}
