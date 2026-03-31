package com.example.auth_user_service.dtos;

public class DeductWalletRequestDTO {
    private Long userId;
    private Long walletId;
    private String currencyType;
    private String recipientUser;
    private String amount;

    public DeductWalletRequestDTO(Long userId, Long walletId, String currencyType, String recipientUser, String amount) {
        this.userId = userId;
        this.walletId = walletId;
        this.currencyType = currencyType;
        this.recipientUser = recipientUser;
        this.amount = amount;
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getWalletId() {
        return this.walletId;
    }

    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }

    public String getCurrencyType() {
        return this.currencyType;
    }

    public void setCurrencyType(String currencyType) {
        this.currencyType = currencyType;
    }

    public String getRecipientUser() {
        return this.recipientUser;
    }

    public void setRecipientUser(String recipientUser) {
        this.recipientUser = recipientUser;
    }

    public String getAmount() {
        return this.amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

}
