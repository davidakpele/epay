package com.pesco.wallet_service.payloads;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateTransferPinRequest {

    private Long userId;
    private Long walletId;
    private String username;
    private String transferPin;

    public CreateTransferPinRequest() {
    }

    public CreateTransferPinRequest(Long userId, Long walletId, String username, String transferPin) {
        this.userId = userId;
        this.walletId = walletId;
        this.username = username;
        this.transferPin = transferPin;
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

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTransferPin() {
        return this.transferPin;
    }

    public void setTransferPin(String transferPin) {
        this.transferPin = transferPin;
    }

}
