package com.epay.admin.service;

import com.epay.domain.virtual_card.entity.CardFeeConfig;
import com.epay.domain.virtual_card.enums.CardType;
import com.epay.domain.virtual_card.repository.CardFeeConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardFeeConfigService {

    private final CardFeeConfigRepository repository;

    public List<CardFeeConfig> getAll() {
        return repository.findAllByOrderByCurrencyCodeAscCardTypeAsc();
    }

    public CardFeeConfig getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "CardFeeConfig not found: id=" + id));
    }

    public List<CardFeeConfig> getByCurrency(String currencyCode) {
        return repository.findByCurrencyCodeIgnoreCase(currencyCode);
    }

    public CardFeeConfig getByCurrencyAndType(String currencyCode, CardType cardType) {
        return repository
                .findByCurrencyCodeIgnoreCaseAndCardType(currencyCode, cardType)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "CardFeeConfig not found: currency=" + currencyCode + " type=" + cardType));
    }

    @Transactional
    public CardFeeConfig update(Long id, BigDecimal feeAmount, String description,
                                Boolean active, Long adminId) {
        CardFeeConfig config = getById(id);

        if (feeAmount != null) {
            if (feeAmount.compareTo(BigDecimal.ZERO) < 0)
                throw new IllegalArgumentException("Fee amount must not be negative");
            config.setFeeAmount(feeAmount);
        }
        if (description != null)  config.setDescription(description);
        if (active != null)       config.setActive(active);

        config.setUpdatedBy(adminId);
        CardFeeConfig saved = repository.save(config);
        log.info("[CardFeeConfig] Updated id={} by adminId={}: fee={} active={}",
                id, adminId, saved.getFeeAmount(), saved.isActive());
        return saved;
    }


    @Transactional
    public CardFeeConfig setActive(Long id, boolean active, Long adminId) {
        getById(id); 
        repository.setActive(id, active, adminId);
        log.info("[CardFeeConfig] id={} {} by adminId={}", id, active ? "enabled" : "disabled", adminId);
        return getById(id);
    }

    @Transactional
    public CardFeeConfig updateFee(Long id, BigDecimal feeAmount, Long adminId) {
        getById(id);
        if (feeAmount == null || feeAmount.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Fee amount must be >= 0");
        repository.updateFeeAmount(id, feeAmount, adminId);
        log.info("[CardFeeConfig] Fee updated id={} amount={} by adminId={}", id, feeAmount, adminId);
        return getById(id);
    }
}
