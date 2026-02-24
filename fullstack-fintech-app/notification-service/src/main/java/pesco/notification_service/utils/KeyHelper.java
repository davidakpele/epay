package pesco.notification_service.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Random;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;
import pesco.notification_service.enums.TransactionType;

@Component
public class KeyHelper {

    @SuppressWarnings("unused")
    private final HttpServletRequest request;

    public KeyHelper(HttpServletRequest request) {
        this.request = request;
    }
    public static String FormatBigDecimal(BigDecimal value) {
        if (value == null) {
            return "0.00";
        }
        try {
            String pattern = "#,##0.00";
            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setGroupingSeparator(',');
            symbols.setDecimalSeparator('.');
            DecimalFormat decimalFormat = new DecimalFormat(pattern, symbols);
            return decimalFormat.format(value);
        } catch (Exception e) {
            return "0.00";
        }
    }

    public static String FormatEnumValue(TransactionType transactionType) {
        // Convert the enum name to a string and replace underscores with spaces
        return transactionType.name().replace("_", " ");
    }

    public static String GenerateOTP() {
        int otpLength = 6;
        Random random = new Random();

        StringBuilder otp = new StringBuilder(otpLength);

        for (int i = 0; i < otpLength; i++) {
            otp.append(random.nextInt(10));
        }

        return otp.toString();
    }

    public static Long GenerateUniquId() {
        long timestamp = System.currentTimeMillis();
        Random random = new Random();
        int randomNumber = 1000 + random.nextInt(9000);
        String combinedId = timestamp + String.valueOf(randomNumber);
        return Long.parseLong(combinedId);
    }
}
