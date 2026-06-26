package com.example.admin_api_service.enums;

public enum LiquidityTransactionType {
    TREASURY_FUNDING,     // admin adds funds
    WITHDRAWAL_RESERVE,   // funds reserved for user withdrawal
    WITHDRAWAL_RELEASE,   // reserved funds released (failed withdrawal)
    WITHDRAWAL_EXECUTED,  // actual debit from system wallet
    SETTLEMENT_IN,        // incoming from provider/bank
    SETTLEMENT_OUT,       // outgoing to provider
    ADJUSTMENT            // manual correction (rare, audited)
, WITHDRAWAL, REBALANCE_IN, REBALANCE_OUT, RESERVE, RELEASE, SETTLEMENT, FUND
}