package pesco.deposit_service.enums;

public enum CurrencyType {
    USD,
    EUR,
    NGN,
    GBP,
    JPY,
    AUD,
    CAD,
    CHF,
    CNY, 
    INR; 

    public static CurrencyType fromString(String value) {
        for (CurrencyType type : CurrencyType.values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown currency type: " + value);
    }
}
