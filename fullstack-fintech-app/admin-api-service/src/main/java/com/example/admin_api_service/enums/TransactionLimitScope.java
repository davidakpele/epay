package com.example.admin_api_service.enums;

public enum TransactionLimitScope {
    GLOBAL,      // Applies to all users regardless of tier
    KYC_TIER,    // Applies to users of a specific KYC tier
    USER         // User-level override via TransactionLimitOverride
}
 