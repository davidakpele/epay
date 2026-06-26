package com.example.admin_api_service.Interfaces;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.admin_api_service.enums.ExchangeRateSource;
import com.example.admin_api_service.models.systemAndConfiguration.ExchangeRateSnapshot;

public interface IExchangeRateSnapshotService {
    ExchangeRateSnapshot publishRate(String baseCurrency, String targetCurrency,
                                     BigDecimal rate, BigDecimal buyRate, BigDecimal sellRate,
                                     BigDecimal spreadPercentage, ExchangeRateSource source,
                                     String providerReference);
 
    ExchangeRateSnapshot manualOverride(String baseCurrency, String targetCurrency,
                                        BigDecimal rate, BigDecimal buyRate, BigDecimal sellRate,
                                        String overriddenBy, String overrideReason,
                                        LocalDateTime expiresAt);
 
    ExchangeRateSnapshot getRateById(String rateId);

    Optional<ExchangeRateSnapshot> getActiveRate(String baseCurrency, String targetCurrency);
 
    BigDecimal getActiveSellRate(String baseCurrency, String targetCurrency);
 
    Page<ExchangeRateSnapshot> getRateHistory(String baseCurrency, String targetCurrency,
                                              Pageable pageable);
 
    Page<ExchangeRateSnapshot> getAllRates(Pageable pageable);
 
    List<ExchangeRateSnapshot> getAllActiveRates();
 
    void deactivateRate(String rateId);
 
    // Convert an amount using the active sell rate
    BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency);
}
