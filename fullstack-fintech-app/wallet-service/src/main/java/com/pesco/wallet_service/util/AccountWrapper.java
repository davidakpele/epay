package com.pesco.wallet_service.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.pesco.wallet_service.enums.Currency;
import com.pesco.wallet_service.models.CurrencyBalanceMapStruct;
import com.pesco.wallet_service.models.Wallet;

@Component
public class AccountWrapper {
    // Helper methods
    public void initializeAllCurrencyWallets(Wallet wallet) {
        List<CurrencyBalanceMapStruct> balances = new ArrayList<>();
        for (Currency currency : Currency.values()) {
            balances.add(new CurrencyBalanceMapStruct(
                    currency.name(),
                    getCurrencySymbol(currency),
                    BigDecimal.ZERO));
        }
        wallet.setBalances(balances);
    }

    private String getCurrencySymbol(Currency currency) {
        switch (currency) {
            case USD:
                return "$";
            case EUR:
                return "€";
            case NGN:
                return "₦";
            case GBP:
                return "£";
            case JPY:
                return "¥";
            case AUD:
                return "A$";
            case CAD:
                return "C$";
            case CHF:
                return "CHF";
            case CNY:
                return "¥";
            case INR:
                return "₹";
            default:
                throw new IllegalArgumentException("Unknown currency type: " + currency);
        }
    }
}
