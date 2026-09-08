package com.epay.domain.auth.enums;

public enum KycDocumentType {

    NATIONAL_ID("National Identity Card"),
    INTERNATIONAL_PASSPORT("International Passport"),
    DRIVERS_LICENSE("Driver's License"),
    VOTERS_CARD("Voter's Card"),
    RESIDENCE_PERMIT("Residence Permit"),

    UTILITY_BILL("Utility Bill"),
    BANK_STATEMENT("Bank Statement"),
    TENANCY_AGREEMENT("Tenancy Agreement"),
    GOVERNMENT_LETTER("Government-issued Letter"),

    SELFIE("Live selfie photo"),
    SELFIE_WITH_ID("Selfie holding ID document"),
    LIVENESS_CHECK("Liveness check video"),

    CAC_CERTIFICATE("Corporate Affairs Commission Certificate"),
    TAX_IDENTIFICATION("Tax Identification Number document"),
    MEMORANDUM_OF_ASSOCIATION("Memorandum and Articles of Association");

    private final String description;

    KycDocumentType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
