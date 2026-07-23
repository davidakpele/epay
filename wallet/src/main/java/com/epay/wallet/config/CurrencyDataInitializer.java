package com.epay.wallet.config;

import com.epay.domain.wallet.entity.SupportedCurrency;
import com.epay.wallet.repository.SupportedCurrencyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CurrencyDataInitializer implements ApplicationRunner {

    private final SupportedCurrencyRepository currencyRepository;

    private static final List<CurrencyDef> CURRENCIES = List.of(
        new CurrencyDef("NGN", "₦",  "Nigerian Naira",          "NG", new BigDecimal("0.00065")),
        new CurrencyDef("USD", "$",  "United States Dollar",    "US", BigDecimal.ONE),
        new CurrencyDef("EUR", "€",  "Euro",                    "EU", new BigDecimal("0.92")),
        new CurrencyDef("GBP", "£",  "British Pound Sterling",  "GB", new BigDecimal("0.79")),
        new CurrencyDef("AUD", "A$", "Australian Dollar",       "AU", new BigDecimal("1.54")),
        new CurrencyDef("JPY", "¥",  "Japanese Yen",            "JP", new BigDecimal("149.50")),
        new CurrencyDef("CAD", "C$", "Canadian Dollar",         "CA", new BigDecimal("1.36")),
        new CurrencyDef("CNY", "¥",  "Chinese Yuan Renminbi",   "CN", new BigDecimal("7.24")),
        new CurrencyDef("CHF", "Fr", "Swiss Franc",             "CH", new BigDecimal("0.90")),
        new CurrencyDef("GHS", "₵",  "Ghanaian Cedi",           "GH", new BigDecimal("12.50"))
    );

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        long existing = currencyRepository.count();
        if (existing > 0) {
            log.info("[CurrencyInit] {} currencies already in DB — skipping seed", existing);
            return;
        }

        for (CurrencyDef def : CURRENCIES) {
            SupportedCurrency currency = SupportedCurrency.builder()
                    .code(def.code())
                    .symbol(def.symbol())
                    .name(def.name())
                    .countryCode(def.countryCode())
                    .exchangeRate(def.exchangeRate())
                    .decimalPlaces(def.code().equals("JPY") ? 0 : 2)
                    .active(true)
                    .defaultEligible(true)
                    .minDeposit(new BigDecimal("1.00"))
                    .minWithdrawal(new BigDecimal("1.00"))
                    .build();
            currencyRepository.save(currency);
        }

        log.info("[CurrencyInit] Seeded {} currencies successfully", CURRENCIES.size());
    }

    private record CurrencyDef(
        String code,
        String symbol,
        String name,
        String countryCode,
        BigDecimal exchangeRate
    ) {}
}
