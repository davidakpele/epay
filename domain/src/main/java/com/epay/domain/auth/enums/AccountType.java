package com.epay.domain.auth.enums;

public enum AccountType {
    INDIVIDUAL("Personal account"),
    BUSINESS("Business account"),
    MERCHANT("Merchant / vendor account");

    private final String description;

    AccountType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
