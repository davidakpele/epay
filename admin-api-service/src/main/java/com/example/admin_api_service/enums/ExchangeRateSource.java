package com.example.admin_api_service.enums;

public enum ExchangeRateSource {
    CBN,              // Central Bank of Nigeria
    INTERBANK,        // Interbank market rate
    PROVIDER_API,     // Third-party provider e.g. Open Exchange Rates, Fixer.io
    MANUAL_OVERRIDE,  // Set manually by an admin
    INTERNAL          // Derived internally from transaction data
}