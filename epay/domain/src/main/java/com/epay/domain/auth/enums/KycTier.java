package com.epay.domain.auth.enums;

public enum KycTier {
    TIER_1("Basic — email and phone verified"),
    TIER_2("Standard — government ID verified"),

    TIER_3("Enhanced — full KYC with proof of address");

    private final String description;

    KycTier(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
