package com.pesco.wallet_service.enums;

public enum Currency {
    USD("$"), EUR("€"), NGN("₦"), GBP("£"),
    JPY("¥"), AUD("A$"), CAD("C$"), CHF("Fr"),
    CNY("¥"), INR("₹");

    private final String symbol;

    Currency(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public static Currency fromString(String value) {
        for (Currency type : Currency.values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown currency type: " + value);
    }
}