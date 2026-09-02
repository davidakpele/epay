package com.epay.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Formats monetary amounts for display in notifications and emails.
 *
 * <p>Rules:
 * <ul>
 *   <li>Always 2 decimal places — never the raw BigDecimal scale (avoids 0E-8, 1000000.00000000).</li>
 *   <li>Thousands separator with comma — 1,000,000.00 not 1000000.00.</li>
 *   <li>{@link #format(String, BigDecimal)} prepends the currency symbol directly against the
 *       number with no space: {@code ₦1,000,000.00}.</li>
 *   <li>{@link #formatWithCode(String, BigDecimal)} puts the currency code before the amount
 *       with a single space: {@code NGN 1,000,000.00} — used in templates that render
 *       the code separately.</li>
 *   <li>Null-safe — a null amount renders as {@code "0.00"} or {@code "₦0.00"}.</li>
 * </ul>
 */
public final class MoneyFormatter {

    private static final DecimalFormat FORMATTER;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ENGLISH);
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        FORMATTER = new DecimalFormat("#,##0.00", symbols);
    }

    private MoneyFormatter() {}

    /**
     * Formats a raw BigDecimal to a human-readable amount string.
     * e.g. {@code new BigDecimal("1000000.00000000")} → {@code "1,000,000.00"}
     */
    public static String amount(BigDecimal value) {
        if (value == null) return "0.00";
        // Normalise scale to 2 dp before formatting so scientific notation is eliminated
        BigDecimal normalised = value.setScale(2, RoundingMode.HALF_UP);
        synchronized (FORMATTER) {
            return FORMATTER.format(normalised);
        }
    }

    /**
     * Formats with the currency symbol directly attached: {@code ₦1,000,000.00}.
     * Use this when you want symbol + amount as a single pre-built string.
     */
    public static String format(String currencySymbol, BigDecimal value) {
        String sym = (currencySymbol != null && !currencySymbol.isBlank()) ? currencySymbol : "";
        return sym + amount(value);
    }

    /**
     * Formats with the currency code followed by a space: {@code NGN 1,000,000.00}.
     * Use this when the template already shows the code separately but the amount
     * still needs to be properly formatted.
     */
    public static String formatWithCode(String currencyCode, BigDecimal value) {
        String code = (currencyCode != null && !currencyCode.isBlank()) ? currencyCode + " " : "";
        return code + amount(value);
    }
}
