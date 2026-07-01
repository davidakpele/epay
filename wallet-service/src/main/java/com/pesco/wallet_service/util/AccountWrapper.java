package com.pesco.wallet_service.util;

import com.pesco.wallet_service.models.CurrencyBalanceMapStruct;
import com.pesco.wallet_service.models.SupportedCurrency;
import com.pesco.wallet_service.models.Wallet;
import com.pesco.wallet_service.services.CurrencyConfigService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Wallet initialisation helper.
 * Previously iterated over the {@code Currency} enum; now fetches enabled
 * currencies dynamically from {@link CurrencyConfigService}.
 */
@Component
public class AccountWrapper {

    private final CurrencyConfigService currencyConfigService;

    public AccountWrapper(CurrencyConfigService currencyConfigService) {
        this.currencyConfigService = currencyConfigService;
    }

    /**
     * Initialises all currently-enabled currency balance rows on a new wallet.
     * Each entry starts at zero.
     */
    public void initializeAllCurrencyWallets(Wallet wallet) {
        List<SupportedCurrency> enabled = currencyConfigService.getEnabledCurrencies();
        List<CurrencyBalanceMapStruct> balances = new ArrayList<>();
        for (SupportedCurrency currency : enabled) {
            balances.add(new CurrencyBalanceMapStruct(
                    currency.getCode(),
                    currency.getSymbol(),
                    BigDecimal.ZERO));
        }
        wallet.setBalances(balances);
    }
}
