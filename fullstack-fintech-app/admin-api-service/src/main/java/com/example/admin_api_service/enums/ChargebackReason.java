package com.example.admin_api_service.enums;

public enum ChargebackReason {
    UNAUTHORISED_TRANSACTION,
    DUPLICATE_TRANSACTION,
    TRANSACTION_NOT_RECEIVED,   // Goods/services not received
    INCORRECT_AMOUNT,
    CREDIT_NOT_PROCESSED,
    SUBSCRIPTION_CANCELLED,
    FRAUD,
    PROCESSING_ERROR,
    ACCOUNT_DEBITED_TWICE,
    TECHNICAL_FALLBACK
}
 