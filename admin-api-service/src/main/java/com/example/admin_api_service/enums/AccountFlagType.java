package com.example.admin_api_service.enums;

public enum AccountFlagType {
    AML_ALERT,
    SANCTION_MATCH,
    FRAUD_SUSPECTED,
    VELOCITY_BREACH,
    UNUSUAL_PATTERN,
    CHARGEBACK_RISK,
    IDENTITY_MISMATCH,
    DUPLICATE_ACCOUNT,
    HIGH_RISK_COUNTRY,
    PEP_MATCH,              // Politically Exposed Person
    NEGATIVE_NEWS,
    KYC_DISCREPANCY,
    MANUAL_REVIEW
}