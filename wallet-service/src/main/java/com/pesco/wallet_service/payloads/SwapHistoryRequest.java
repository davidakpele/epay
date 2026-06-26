package com.pesco.wallet_service.payloads;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class SwapHistoryRequest {

    // ── existing fields ────────────────────────────────────────────────
    private BigDecimal amount;           // → GrossAmount
    private BigDecimal previousBalance;  // → PreviousBalance
    private BigDecimal available;        // → AvailableBalance
    private String description;
    private String accountHolder;        // was userFullName → AccountHolder
    private Long userId;
    private String currencyType;         // toCurrency → CurrencyType
    private String transactionType;      // "SWAP" → Type (must match C# enum)
    private Long walletId;

    // ── previously missing fields ──────────────────────────────────────
    private BigDecimal exchangeRate;     // → ExchangeRate
    private String originalCurrency;     // fromCurrency → OriginalCurrency
    private BigDecimal feeAmount;        // → FeeAmount
    private BigDecimal netAmount;        // finalAmount (post-fee) → NetAmount
    private String status;              // "COMPLETED" → Status

    public SwapHistoryRequest() {
    }

    public SwapHistoryRequest(
            BigDecimal amount,
            BigDecimal previousBalance,
            BigDecimal available,
            String description,
            String accountHolder,
            Long userId,
            String currencyType,
            String transactionType,
            Long walletId,
            BigDecimal exchangeRate,
            String originalCurrency,
            BigDecimal feeAmount,
            BigDecimal netAmount,
            String status) {
        this.amount = amount;
        this.previousBalance = previousBalance;
        this.available = available;
        this.description = description;
        this.accountHolder = accountHolder;
        this.userId = userId;
        this.currencyType = currencyType;
        this.transactionType = transactionType;
        this.walletId = walletId;
        this.exchangeRate = exchangeRate;
        this.originalCurrency = originalCurrency;
        this.feeAmount = feeAmount;
        this.netAmount = netAmount;
        this.status = status;
    }

    // ── existing getters/setters ───────────────────────────────────────
    public BigDecimal getAmount() { return this.amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getPreviousBalance() { return this.previousBalance; }
    public void setPreviousBalance(BigDecimal previousBalance) { this.previousBalance = previousBalance; }

    public BigDecimal getAvailable() { return this.available; }
    public void setAvailable(BigDecimal available) { this.available = available; }

    public String getDescription() { return this.description; }
    public void setDescription(String description) { this.description = description; }

    public String getAccountHolder() { return this.accountHolder; }
    public void setAccountHolder(String accountHolder) { this.accountHolder = accountHolder; }

    public Long getUserId() { return this.userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getCurrencyType() { return this.currencyType; }
    public void setCurrencyType(String currencyType) { this.currencyType = currencyType; }

    public String getTransactionType() { return this.transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public Long getWalletId() { return this.walletId; }
    public void setWalletId(Long walletId) { this.walletId = walletId; }

    // ── new getters/setters ────────────────────────────────────────────
    public BigDecimal getExchangeRate() { return this.exchangeRate; }
    public void setExchangeRate(BigDecimal exchangeRate) { this.exchangeRate = exchangeRate; }

    public String getOriginalCurrency() { return this.originalCurrency; }
    public void setOriginalCurrency(String originalCurrency) { this.originalCurrency = originalCurrency; }

    public BigDecimal getFeeAmount() { return this.feeAmount; }
    public void setFeeAmount(BigDecimal feeAmount) { this.feeAmount = feeAmount; }

    public BigDecimal getNetAmount() { return this.netAmount; }
    public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }

    public String getStatus() { return this.status; }
    public void setStatus(String status) { this.status = status; }
}