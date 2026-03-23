package com.example.admin_api_service.services;

import com.example.admin_api_service.Interfaces.IFeeConfigurationService;
import com.example.admin_api_service.Interfaces.IFeeRuleService;
import com.example.admin_api_service.enums.FeeCalculationMethod;
import com.example.admin_api_service.enums.FeeConfigurationStatus;
import com.example.admin_api_service.enums.FeeConfigurationType;
import com.example.admin_api_service.exceptions.BadRequestException;
import com.example.admin_api_service.exceptions.ConflictException;
import com.example.admin_api_service.exceptions.ResourceNotFoundException;
import com.example.admin_api_service.models.feeAndLimits.FeeConfiguration;
import com.example.admin_api_service.models.feeAndLimits.FeeRule;
import org.springframework.context.annotation.Lazy;
import com.example.admin_api_service.repository.FeeConfigurationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class FeeConfigurationServiceImpl implements IFeeConfigurationService {

    private final FeeConfigurationRepository feeConfigurationRepository;
    private final IFeeRuleService feeRuleService;

    public FeeConfigurationServiceImpl(FeeConfigurationRepository feeConfigurationRepository,
                                        @Lazy IFeeRuleService feeRuleService) {
        this.feeConfigurationRepository = feeConfigurationRepository;
        this.feeRuleService = feeRuleService;
    }

    @Override
    public FeeConfiguration createConfiguration(FeeConfiguration config, String createdBy) {
        if (feeConfigurationRepository.existsByCode(config.getCode())) {
            throw new ConflictException("Fee configuration with code '" + config.getCode() + "' already exists");
        }
        config.setCreatedBy(createdBy);
        config.setStatus(FeeConfigurationStatus.ACTIVE);
        if (config.getEffectiveFrom() == null) {
            config.setEffectiveFrom(LocalDateTime.now());
        }
        return feeConfigurationRepository.save(config);
    }

    @Override
    public FeeConfiguration updateConfiguration(String configId, FeeConfiguration updated, String updatedBy) {
        FeeConfiguration existing = getConfigurationById(configId);

        if (!existing.getCode().equals(updated.getCode())
                && feeConfigurationRepository.existsByCode(updated.getCode())) {
            throw new ConflictException("Fee configuration with code '" + updated.getCode() + "' already exists");
        }

        existing.setCode(updated.getCode());
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setType(updated.getType());
        existing.setTransactionType(updated.getTransactionType());
        existing.setChannel(updated.getChannel());
        existing.setCurrency(updated.getCurrency());
        existing.setKycTier(updated.getKycTier());
        existing.setFeeCollectionWalletId(updated.getFeeCollectionWalletId());
        existing.setVatApplicable(updated.isVatApplicable());
        existing.setVatRate(updated.getVatRate());
        existing.setEffectiveFrom(updated.getEffectiveFrom());
        existing.setEffectiveTo(updated.getEffectiveTo());
        existing.setUpdatedBy(updatedBy);
        existing.setUpdatedOn(LocalDateTime.now());
        return feeConfigurationRepository.save(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public FeeConfiguration getConfigurationById(String configId) {
        return feeConfigurationRepository.findById(configId)
                .orElseThrow(() -> new ResourceNotFoundException("FeeConfiguration", "id", configId));
    }

    @Override
    @Transactional(readOnly = true)
    public FeeConfiguration getConfigurationByCode(String code) {
        return feeConfigurationRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("FeeConfiguration", "code", code));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FeeConfiguration> getAllConfigurations(Pageable pageable) {
        return feeConfigurationRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeeConfiguration> getActiveConfigurations() {
        return feeConfigurationRepository.findAllByStatus(FeeConfigurationStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeeConfiguration> getConfigurationsByType(FeeConfigurationType type) {
        return feeConfigurationRepository.findAllByTypeAndStatus(type, FeeConfigurationStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public FeeConfiguration resolveConfiguration(FeeConfigurationType type, String channel,
                                                  String currency, String kycTier) {
        LocalDateTime now = LocalDateTime.now();

        // Priority: most specific match first (type + channel + currency + kycTier)
        // Fall back to progressively less specific matches
        return feeConfigurationRepository
                .findBestMatch(type, channel, currency, kycTier, now)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "FeeConfiguration", "type+channel+currency+kycTier",
                        type + "+" + channel + "+" + currency + "+" + kycTier));
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateFee(FeeConfigurationType type, String channel,
                                   String currency, String kycTier,
                                   BigDecimal transactionAmount) {
        FeeConfiguration config = resolveConfiguration(type, channel, currency, kycTier);
        FeeRule rule = feeRuleService.resolveRuleForAmount(config.getId(), transactionAmount);
        return computeFee(rule, transactionAmount, config.isVatApplicable(), config.getVatRate());
    }

    @Override
    public void activateConfiguration(String configId, String updatedBy) {
        FeeConfiguration config = getConfigurationById(configId);
        config.setStatus(FeeConfigurationStatus.ACTIVE);
        config.setUpdatedBy(updatedBy);
        config.setUpdatedOn(LocalDateTime.now());
        feeConfigurationRepository.save(config);
    }

    @Override
    public void deactivateConfiguration(String configId, String updatedBy) {
        FeeConfiguration config = getConfigurationById(configId);
        config.setStatus(FeeConfigurationStatus.INACTIVE);
        config.setUpdatedBy(updatedBy);
        config.setUpdatedOn(LocalDateTime.now());
        feeConfigurationRepository.save(config);
    }

    @Override
    public FeeConfiguration supersede(String oldConfigId, FeeConfiguration newConfig, String updatedBy) {
        FeeConfiguration old = getConfigurationById(oldConfigId);

        // Mark old config as superseded
        old.setStatus(FeeConfigurationStatus.SUPERSEDED);
        old.setEffectiveTo(LocalDateTime.now());
        old.setUpdatedBy(updatedBy);
        old.setUpdatedOn(LocalDateTime.now());
        feeConfigurationRepository.save(old);

        // Create new config inheriting code + type context
        newConfig.setCreatedBy(updatedBy);
        newConfig.setStatus(FeeConfigurationStatus.ACTIVE);
        newConfig.setEffectiveFrom(LocalDateTime.now());
        return feeConfigurationRepository.save(newConfig);
    }

    @Override
    public void deleteConfiguration(String configId) {
        FeeConfiguration config = getConfigurationById(configId);
        if (config.getStatus() == FeeConfigurationStatus.ACTIVE) {
            throw new ConflictException("Active fee configurations cannot be deleted. Deactivate first.");
        }
        feeConfigurationRepository.delete(config);
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private BigDecimal computeFee(FeeRule rule, BigDecimal amount,
                                  boolean vatApplicable, BigDecimal vatRate) {
        if (rule.isWaived()) return BigDecimal.ZERO;

        BigDecimal fee;
        FeeCalculationMethod method = rule.getCalculationMethod();

        fee = switch (method) {
            case FLAT -> rule.getFlatAmount() != null ? rule.getFlatAmount() : BigDecimal.ZERO;
            case PERCENTAGE -> amount.multiply(rule.getPercentageRate())
                    .setScale(4, RoundingMode.HALF_UP);
            case FLAT_PLUS_PERCENTAGE -> {
                BigDecimal flat = rule.getFlatAmount() != null ? rule.getFlatAmount() : BigDecimal.ZERO;
                BigDecimal pct = rule.getPercentageRate() != null
                        ? amount.multiply(rule.getPercentageRate()).setScale(4, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;
                yield flat.add(pct);
            }
            case WAIVED -> BigDecimal.ZERO;
            default -> throw new BadRequestException("Unsupported fee calculation method: " + method);
        };

        // Apply min/max caps
        if (rule.getMinFee() != null && fee.compareTo(rule.getMinFee()) < 0) {
            fee = rule.getMinFee();
        }
        if (rule.getMaxFee() != null && fee.compareTo(rule.getMaxFee()) > 0) {
            fee = rule.getMaxFee();
        }

        // Apply VAT on top
        if (vatApplicable && vatRate != null && vatRate.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal vat = fee.multiply(vatRate).setScale(4, RoundingMode.HALF_UP);
            fee = fee.add(vat);
        }

        return fee.setScale(4, RoundingMode.HALF_UP);
    }
}
