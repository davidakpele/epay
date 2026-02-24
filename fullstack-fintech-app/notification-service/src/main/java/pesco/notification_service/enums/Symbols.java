package pesco.notification_service.enums;

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

    public static String getCurrencyCodeBySymbol(String inputSymbol) {
        for (Symbols s : Symbols.values()) {
            if (s.getSymbol().equals(inputSymbol)) {
                return s.name(); 
            }
        }
        return "USD"; 
    }
}
