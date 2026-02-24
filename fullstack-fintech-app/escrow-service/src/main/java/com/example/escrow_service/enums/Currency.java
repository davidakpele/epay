package com.example.escrow_service.enums;

public enum Currency {
    USD, EUR, NGN, GBP, JPY, AUD, CAD, CHF, CNY, INR;

    public static Currency fromString(String value) {
        for (Currency type : Currency.values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown currency type: " + value);
    }
}