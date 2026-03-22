package com.example.admin_api_service.enums;

public enum CurrencyStatus {
    ACTIVE,
    INACTIVE,
    SUSPENDED,     // Temporarily blocked e.g. due to regulatory directive
    DEPRECATED     // No longer supported — existing balances only
}
 