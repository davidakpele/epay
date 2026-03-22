package com.example.admin_api_service.enums;

public enum RiskRuleType {
    VELOCITY,              // Transaction frequency or cumulative amount check
    SINGLE_TRANSACTION,    // Single transaction threshold check
    PATTERN,               // Behavioural pattern check (e.g. round amounts, unusual hours)
    COUNTERPARTY,          // Counterparty risk check
    GEO,                   // Geographic / country risk check
    SANCTION,              // Sanction list match trigger
    KYC,                   // KYC status-based rule
    DORMANCY,              // Dormant account reactivation rule
    COMPOSITE              // Combination of multiple rule types
}
 
