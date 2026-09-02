package com.epay.domain.auth.enums;

public enum KycTier {

    /**
     * Email + phone verified only.
     * Lowest transaction limits.
     */
    TIER_1("Basic — email and phone verified"),

    /**
     * Government-issued photo ID verified.
     * Mid-tier transaction limits.
     */
    TIER_2("Standard — government ID verified"),

    /**
     * Full KYC: ID + proof of address + selfie.
     * Highest transaction limits.
     */
    TIER_3("Enhanced — full KYC with proof of address");

    private final String description;

    KycTier(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
