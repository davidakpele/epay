package com.pesco.wallet_service.payloads;


public class WalletBalanceResponseDTO {

    private Long walletId;
    private String currencyCode;
    private String symbol;
    private String balance;


    public WalletBalanceResponseDTO() {
    }

    public WalletBalanceResponseDTO(Long walletId, String currencyCode, String symbol, String balance) {
        this.walletId = walletId;
        this.currencyCode = currencyCode;
        this.symbol = symbol;
        this.balance = balance;
    }

    public Long getWalletId() {
        return this.walletId;
    }

    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }

    public String getCurrencyCode() {
        return this.currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getSymbol() {
        return this.symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getBalance() {
        return this.balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }
    
}
