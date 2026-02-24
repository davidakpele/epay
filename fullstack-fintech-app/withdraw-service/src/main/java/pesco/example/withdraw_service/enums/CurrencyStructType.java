package pesco.example.withdraw_service.enums;

public enum CurrencyStructType {
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

    public static CurrencyStructType fromString(String value) {
        for (CurrencyStructType type : CurrencyStructType.values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown currency type: " + value);
    }
}
