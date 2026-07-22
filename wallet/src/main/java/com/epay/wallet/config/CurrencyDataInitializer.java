package com.epay.wallet.config;

import com.epay.domain.wallet.entity.SupportedCurrency;
import com.epay.wallet.repository.SupportedCurrencyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Seeds the supported_currencies table on startup if empty.
 * Safe to run multiple times — only inserts if the row doesn't already exist.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CurrencyDataInitializer implements ApplicationRunner {

    private final SupportedCurrencyRepository currencyRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedIfAbsent("NGN", "₦",  "Nigerian Naira");
        seedIfAbsent("USD", "$",  "United States Dollar");
        seedIfAbsent("GBP", "£",  "British Pound Sterling");
        seedIfAbsent("EUR", "€",  "Euro");
        seedIfAbsent("GHS", "₵",  "Ghanaian Cedi");
        seedIfAbsent("KES", "KSh","Kenyan Shilling");
        seedIfAbsent("ZAR", "R",  "South African Rand");
        seedIfAbsent("XOF", "CFA","West African CFA Franc");
    }

    private void seedIfAbsent(String code, String symbol, String name) {
        if (currencyRepository.findByCodeIgnoreCase(code).isEmpty()) {
            SupportedCurrency currency = SupportedCurrency.builder()
                    .code(code)
                    .symbol(symbol)
                    .name(name)
                    .active(true)
                    .defaultEligible(true)
                    .exchangeRate(java.math.BigDecimal.ONE)
                    .decimalPlaces(2)
                    .build();
            currencyRepository.save(currency);
            log.info("[CurrencyInit] Seeded currency: {} ({})", code, symbol);
        }
    }
}
