package com.example.admin_api_service.services;

import com.example.admin_api_service.Interfaces.IExchangeRateSnapshotService;
import com.example.admin_api_service.enums.ExchangeRateSource;
import com.example.admin_api_service.exceptions.BadRequestException;
import com.example.admin_api_service.exceptions.ResourceNotFoundException;
import com.example.admin_api_service.models.systemAndConfiguration.ExchangeRateSnapshot;
import com.example.admin_api_service.repository.ExchangeRateSnapshotRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ExchangeRateSnapshotServiceImpl implements IExchangeRateSnapshotService {

    private final ExchangeRateSnapshotRepository rateRepository;

    public ExchangeRateSnapshotServiceImpl(ExchangeRateSnapshotRepository rateRepository) {
        this.rateRepository = rateRepository;
    }

    @Override
    public ExchangeRateSnapshot publishRate(String baseCurrency, String targetCurrency,
                                             BigDecimal rate, BigDecimal buyRate,
                                             BigDecimal sellRate, BigDecimal spreadPercentage,
                                             ExchangeRateSource source, String providerReference) {
        // Deactivate any existing active rate for this pair
        rateRepository.findActiveRate(baseCurrency, targetCurrency, LocalDateTime.now())
                .ifPresent(existing -> {
                    existing.setActive(false);
                    rateRepository.save(existing);
                });

        ExchangeRateSnapshot snapshot = new ExchangeRateSnapshot();
        snapshot.setBaseCurrency(baseCurrency);
        snapshot.setTargetCurrency(targetCurrency);
        snapshot.setRate(rate);
        snapshot.setBuyRate(buyRate);
        snapshot.setSellRate(sellRate);
        snapshot.setSpreadPercentage(spreadPercentage);
        snapshot.setSource(source);
        snapshot.setProviderReference(providerReference);
        snapshot.setActive(true);
        snapshot.setManualOverride(false);
        snapshot.setEffectiveAt(LocalDateTime.now());
        return rateRepository.save(snapshot);
    }

    @Override
    public ExchangeRateSnapshot manualOverride(String baseCurrency, String targetCurrency,
                                                BigDecimal rate, BigDecimal buyRate,
                                                BigDecimal sellRate, String overriddenBy,
                                                String overrideReason, LocalDateTime expiresAt) {
        // Deactivate existing rate
        rateRepository.findActiveRate(baseCurrency, targetCurrency, LocalDateTime.now())
                .ifPresent(existing -> {
                    existing.setActive(false);
                    rateRepository.save(existing);
                });

        ExchangeRateSnapshot snapshot = new ExchangeRateSnapshot();
        snapshot.setBaseCurrency(baseCurrency);
        snapshot.setTargetCurrency(targetCurrency);
        snapshot.setRate(rate);
        snapshot.setBuyRate(buyRate);
        snapshot.setSellRate(sellRate);
        snapshot.setSource(ExchangeRateSource.MANUAL_OVERRIDE);
        snapshot.setActive(true);
        snapshot.setManualOverride(true);
        snapshot.setOverriddenBy(overriddenBy);
        snapshot.setOverrideReason(overrideReason);
        snapshot.setEffectiveAt(LocalDateTime.now());
        snapshot.setExpiresAt(expiresAt);
        return rateRepository.save(snapshot);
    }

    @Override
    @Transactional(readOnly = true)
    public ExchangeRateSnapshot getRateById(String rateId) {
        return rateRepository.findById(rateId)
                .orElseThrow(() -> new ResourceNotFoundException("ExchangeRateSnapshot", "id", rateId));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ExchangeRateSnapshot> getActiveRate(String baseCurrency, String targetCurrency) {
        return rateRepository.findActiveRate(baseCurrency, targetCurrency, LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getActiveSellRate(String baseCurrency, String targetCurrency) {
        return getActiveRate(baseCurrency, targetCurrency)
                .map(r -> r.getSellRate() != null ? r.getSellRate() : r.getRate())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ExchangeRateSnapshot", "pair", baseCurrency + "/" + targetCurrency));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExchangeRateSnapshot> getRateHistory(String baseCurrency, String targetCurrency,
                                                     Pageable pageable) {
        return rateRepository.findAllByBaseCurrencyAndTargetCurrencyOrderByEffectiveAtDesc(
                baseCurrency, targetCurrency, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExchangeRateSnapshot> getAllRates(Pageable pageable) {
        return rateRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExchangeRateSnapshot> getAllActiveRates() {
        return rateRepository.findAllByIsActiveTrueAndEffectiveAtBeforeAndExpiresAtAfterOrExpiresAtIsNull(
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Override
    public void deactivateRate(String rateId) {
        ExchangeRateSnapshot rate = getRateById(rateId);
        rate.setActive(false);
        rateRepository.save(rate);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency) {
        if (fromCurrency.equalsIgnoreCase(toCurrency)) return amount;
        BigDecimal sellRate = getActiveSellRate(fromCurrency, toCurrency);
        return amount.multiply(sellRate).setScale(4, RoundingMode.HALF_UP);
    }
}
