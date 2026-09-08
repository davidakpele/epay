package com.epay.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;


public final class MoneyFormatter {

    private static final DecimalFormat FORMATTER;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ENGLISH);
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        FORMATTER = new DecimalFormat("#,##0.00", symbols);
    }

    private MoneyFormatter() {}

    public static String amount(BigDecimal value) {
        if (value == null) return "0.00";
        BigDecimal normalised = value.setScale(2, RoundingMode.HALF_UP);
        synchronized (FORMATTER) {
            return FORMATTER.format(normalised);
        }
    }

    public static String format(String currencySymbol, BigDecimal value) {
        String sym = (currencySymbol != null && !currencySymbol.isBlank()) ? currencySymbol : "";
        return sym + amount(value);
    }

    public static String formatWithCode(String currencyCode, BigDecimal value) {
        String code = (currencyCode != null && !currencyCode.isBlank()) ? currencyCode + " " : "";
        return code + amount(value);
    }
}
