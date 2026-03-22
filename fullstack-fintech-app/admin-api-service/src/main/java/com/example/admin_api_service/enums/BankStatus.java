package com.example.admin_api_service.enums;

public enum BankStatus {
    ACTIVE,
    INACTIVE,
    TRANSFER_DISABLED,   // Transfers blocked but bank still exists in the system
    DEGRADED,            // Experiencing intermittent failures
    SUSPENDED            // Suspended by regulator or internal decision
}