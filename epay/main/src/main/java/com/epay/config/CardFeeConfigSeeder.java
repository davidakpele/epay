package com.epay.config;

import com.epay.domain.virtual_card.entity.CardFeeConfig;
import com.epay.domain.virtual_card.enums.CardType;
import com.epay.domain.virtual_card.repository.CardFeeConfigRepository;
import com.epay.domain.wallet.entity.SupportedCurrency;
import com.epay.wallet.repository.SupportedCurrencyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;


@Slf4j
@Component
@Order(3)
@RequiredArgsConstructor
public class CardFeeConfigSeeder implements ApplicationRunner {

    private static final BigDecimal USD_VISA_FEE   = new BigDecimal("1.00");
    private static final BigDecimal USD_MASTER_FEE = new BigDecimal("5.00");

    private static final String[] CURRENCY_CODES = {
        "NGN", "USD", "AUD", "JPY", "EUR", "GBP", "CAD", "CNY", "CHF", "GHS"
    };

    private final CardFeeConfigRepository   feeConfigRepository;
    private final SupportedCurrencyRepository currencyRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int inserted = 0;
        int skipped  = 0;

        for (String code : CURRENCY_CODES) {
            SupportedCurrency currency = currencyRepository
                    .findByCodeIgnoreCase(code)
                    .orElse(null);

            if (currency == null) {
                log.warn("[CardFeeSeeder] Currency '{}' not found in supported_currencies — skipping", code);
                skipped += 2;
                continue;
            }

            BigDecimal rate = currency.getExchangeRate();
            if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("[CardFeeSeeder] Invalid exchange rate for '{}' — skipping", code);
                skipped += 2;
                continue;
            }

            for (CardType cardType : CardType.values()) {
                if (feeConfigRepository.existsByCurrencyCodeIgnoreCaseAndCardType(code, cardType)) {
                    log.debug("[CardFeeSeeder] Fee already exists for {}/{} — skipping", code, cardType);
                    skipped++;
                    continue;
                }

                BigDecimal usdBase = cardType == CardType.VISA ? USD_VISA_FEE : USD_MASTER_FEE;
                BigDecimal fee     = convertFromUsd(usdBase, rate);

                CardFeeConfig config = CardFeeConfig.builder()
                        .currencyCode(code.toUpperCase())
                        .cardType(cardType)
                        .feeAmount(fee)
                        .description(buildDescription(cardType, code, fee, currency.getSymbol()))
                        .active(true)
                        .build();

                feeConfigRepository.save(config);
                inserted++;
                log.debug("[CardFeeSeeder] Seeded {}/{} → {} {}",
                        code, cardType, currency.getSymbol(), fee);
            }
        }

        if (inserted > 0) {
            log.info("[CardFeeSeeder] ✓ Seeded {} card fee config record(s) ({} already existed)",
                    inserted, skipped);
        } else {
            log.info("[CardFeeSeeder] All {} card fee config records already exist — skipping", skipped);
        }
    }


    private BigDecimal convertFromUsd(BigDecimal usdAmount, BigDecimal exchangeRate) {
        return usdAmount
                .divide(exchangeRate, new MathContext(20, RoundingMode.HALF_UP))
                .setScale(4, RoundingMode.HALF_UP);
    }

    private String buildDescription(CardType cardType, String currencyCode,
                                    BigDecimal fee, String symbol) {
        String network = cardType == CardType.VISA ? "Visa" : "Mastercard";
        return String.format("%s virtual card issuance fee (%s %s)",
                network, symbol, fee.setScale(2, RoundingMode.HALF_UP));
    }
}
