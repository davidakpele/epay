package com.example.admin_api_service.enums;

public enum FeeCalculationMethod {
    FLAT,                   // Fixed amount regardless of transaction size
    PERCENTAGE,             // % of transaction amount
    FLAT_PLUS_PERCENTAGE,   // Fixed amount + % of transaction amount
    TIERED,                 // Different rate per amount band (resolved via FeeRule rows)
    WAIVED                  // No fee
}