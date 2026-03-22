package com.example.admin_api_service.enums;

public enum AmlCaseType {
    SUSPICIOUS_TRANSACTION,
    VELOCITY_BREACH,
    STRUCTURING,           // Breaking up large transactions to avoid reporting thresholds
    LAYERING,              // Moving funds through multiple accounts to obscure origin
    INTEGRATION,           // Re-introducing laundered funds into the economy
    SANCTION_MATCH,
    PEP_ACTIVITY,          // Politically Exposed Person activity
    FRAUD_SUSPECTED,
    UNUSUAL_PATTERN,
    MANUAL_REVIEW
}
