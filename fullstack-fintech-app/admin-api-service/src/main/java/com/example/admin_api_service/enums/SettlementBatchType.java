package com.example.admin_api_service.enums;

public enum SettlementBatchType {
    BANK_TRANSFER,
    CARD_SETTLEMENT,
    INTERBANK,
    INTERNAL_SWEEP,       // Moving funds between system wallets
    NIP_SETTLEMENT,       // NIP (NIBSS Instant Payment) settlement
    USSD_SETTLEMENT,
    PARTNER_PAYOUT,       // Payouts to external partners or merchants
    FX_SETTLEMENT
}
 