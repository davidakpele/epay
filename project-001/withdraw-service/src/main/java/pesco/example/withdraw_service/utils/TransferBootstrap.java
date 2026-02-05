package pesco.example.withdraw_service.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import org.springframework.stereotype.Service;

@Service
public class TransferBootstrap {
    
    public BigDecimal calculateFee(BigDecimal amount) {
        BigDecimal fee = BigDecimal.ZERO;

        if (amount.compareTo(new BigDecimal("1000")) <= 0) {
            fee = amount.multiply(new BigDecimal("0.005")); // 0.5%
        } else if (amount.compareTo(new BigDecimal("10000")) <= 0) {
            fee = amount.multiply(new BigDecimal("0.0045")); // 0.45%
        } else if (amount.compareTo(new BigDecimal("20000")) <= 0) {
            fee = amount.multiply(new BigDecimal("0.003")); // 0.3%
        } else if (amount.compareTo(new BigDecimal("100000")) <= 0) {
            fee = amount.multiply(new BigDecimal("0.00085")); // 0.085%
        } else if (amount.compareTo(new BigDecimal("1000000")) <= 0) {
            fee = amount.multiply(new BigDecimal("0.000085")); // 0.0085%
        }

        return fee.setScale(2, RoundingMode.HALF_UP);
    }

    public String formatBigDecimal(BigDecimal amount) {
        String pattern = "#,##0.00";
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat(pattern, symbols);
        return decimalFormat.format(amount);
    }
    
}