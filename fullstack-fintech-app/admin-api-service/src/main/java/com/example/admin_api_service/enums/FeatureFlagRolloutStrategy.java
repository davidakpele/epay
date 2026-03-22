package com.example.admin_api_service.enums;

public enum FeatureFlagRolloutStrategy {
    ALL_OR_NOTHING,       // Enabled for everyone or no one
    PERCENTAGE_ROLLOUT,   // Enabled for a percentage of users
    USER_WHITELIST,       // Enabled only for specific user IDs
    KYC_TIER,             // Enabled only for specific KYC tiers
    CHANNEL,              // Enabled only for specific channels
    INTERNAL_ONLY         // Enabled only for internal/admin users
}
 