package com.epay.domain.auth.enums;

public enum TokenPurpose {
    EMAIL_VERIFICATION("Verify email address on registration"),
    PHONE_VERIFICATION("Verify phone number"),
    TWO_FACTOR_AUTH("Two-factor authentication challenge"),
    PASSWORD_RESET("Reset account password"),
    TRANSACTION_AUTHORIZATION("Authorize a high-value transaction");

    private final String description;

    TokenPurpose(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
