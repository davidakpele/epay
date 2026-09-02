package com.epay.domain.developer.enums;

public enum WebhookEventType {
    // Wallet events
    WALLET_CREDITED,
    WALLET_DEBITED,
    WALLET_FROZEN,
    WALLET_UNFROZEN,

    // Transaction events
    TRANSACTION_COMPLETED,
    TRANSACTION_FAILED,
    TRANSACTION_REVERSED,

    // Deposit events
    DEPOSIT_INITIATED,
    DEPOSIT_COMPLETED,
    DEPOSIT_FAILED,

    // Withdrawal events
    WITHDRAWAL_INITIATED,
    WITHDRAWAL_COMPLETED,
    WITHDRAWAL_FAILED,
    WITHDRAWAL_REVERSED,

    // KYC events
    KYC_SUBMITTED,
    KYC_APPROVED,
    KYC_REJECTED,

    // Account events
    ACCOUNT_CREATED,
    ACCOUNT_BLOCKED,
    ACCOUNT_UNBLOCKED
}
