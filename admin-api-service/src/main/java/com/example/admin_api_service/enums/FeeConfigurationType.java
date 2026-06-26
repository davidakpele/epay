package com.example.admin_api_service.enums;

public enum FeeConfigurationType {
    TRANSFER_INBOUND,
    TRANSFER_OUTBOUND,
    TRANSFER_INTERNAL,       // Wallet-to-wallet within the platform
    WITHDRAWAL,
    DEPOSIT,
    BILL_PAYMENT,
    AIRTIME_PURCHASE,
    DATA_PURCHASE,
    CARD_TRANSACTION,
    USSD_TRANSACTION,
    FX_CONVERSION,
    REVERSAL,
    CHARGEBACK,
    MAINTENANCE             // Periodic maintenance/account fee
}
 
