package com.epay.domain.auth.enums;

public enum KycStatus {

    NOT_SUBMITTED("No KYC documents submitted yet"),
    SUBMITTED("Documents submitted, pending review"),
    UNDER_REVIEW("Documents are being reviewed by compliance"),
    APPROVED("KYC fully approved"),
    REJECTED("KYC rejected — resubmission required"),
    EXPIRED("KYC approval has expired and must be renewed"),
    SUSPENDED("KYC suspended due to suspicious activity");

    private final String description;

    KycStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
