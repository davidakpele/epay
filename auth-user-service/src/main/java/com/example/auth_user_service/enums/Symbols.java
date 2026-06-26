package com.example.auth_user_service.enums;

public enum Symbols {
    USD("$"),
    EUR("€"),
    NGN("₦"),
    GBP("£"),
    JPY("¥"),
    AUD("A$"),
    CAD("C$"),
    CHF("CHF"),
    CNY("¥"),
    INR("₹");

    private final String symbol;

    Symbols(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}