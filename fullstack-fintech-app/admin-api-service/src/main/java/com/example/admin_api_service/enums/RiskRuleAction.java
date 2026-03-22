package com.example.admin_api_service.enums;

public enum RiskRuleAction {
    SCORE_ONLY,            // Add to risk score with no further action
    RAISE_ALERT,           // Create an AmlAlert
    RAISE_FLAG,            // Create an AccountFlag
    BLOCK_TRANSACTION,     // Block the transaction immediately
    FREEZE_WALLET,         // Trigger a wallet freeze
    RESTRICT_ACCOUNT,      // Apply an account restriction
    NOTIFY_COMPLIANCE,     // Send notification to compliance team
    REQUIRE_REVIEW         // Hold transaction pending manual review
}
