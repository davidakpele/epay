package com.example.admin_api_service.services;

import com.example.admin_api_service.Interfaces.IFeeConfigurationService;
import com.example.admin_api_service.Interfaces.IFeeRuleService;
import com.example.admin_api_service.exceptions.BadRequestException;
import com.example.admin_api_service.exceptions.ConflictException;
import com.example.admin_api_service.exceptions.ResourceNotFoundException;
import com.example.admin_api_service.models.feeAndLimits.FeeRule;
import com.example.admin_api_service.repository.FeeRuleRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class FeeRuleServiceImpl implements IFeeRuleService {

    private final FeeRuleRepository feeRuleRepository;
    private final IFeeConfigurationService feeConfigurationService;

    public FeeRuleServiceImpl(FeeRuleRepository feeRuleRepository, @Lazy IFeeConfigurationService feeConfigurationService) {
        this.feeRuleRepository = feeRuleRepository;
        this.feeConfigurationService = feeConfigurationService;
    }

    @Override
    public FeeRule createRule(String feeConfigurationId, FeeRule rule, String createdBy) {
        feeConfigurationService.getConfigurationById(feeConfigurationId);
        validateBand(feeConfigurationId, rule, null);
        rule.setFeeConfigurationId(feeConfigurationId);
        rule.setCreatedBy(createdBy);
        return feeRuleRepository.save(rule);
    }

    @Override
    public FeeRule updateRule(String ruleId, FeeRule updated, String updatedBy) {
        FeeRule existing = getRuleById(ruleId);
        validateBand(existing.getFeeConfigurationId(), updated, ruleId);

        existing.setMinAmount(updated.getMinAmount());
        existing.setMaxAmount(updated.getMaxAmount());
        existing.setCalculationMethod(updated.getCalculationMethod());
        existing.setFlatAmount(updated.getFlatAmount());
        existing.setPercentageRate(updated.getPercentageRate());
        existing.setMinFee(updated.getMinFee());
        existing.setMaxFee(updated.getMaxFee());
        existing.setWaived(updated.isWaived());
        existing.setUpdatedBy(updatedBy);
        existing.setUpdatedOn(LocalDateTime.now());
        return feeRuleRepository.save(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public FeeRule getRuleById(String ruleId) {
        return feeRuleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("FeeRule", "id", ruleId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeeRule> getRulesForConfiguration(String feeConfigurationId) {
        return feeRuleRepository.findAllByFeeConfigurationIdOrderByMinAmountAsc(feeConfigurationId);
    }

    @Override
    @Transactional(readOnly = true)
    public FeeRule resolveRuleForAmount(String feeConfigurationId, BigDecimal amount) {
        return feeRuleRepository
                .findMatchingRule(feeConfigurationId, amount)
                .orElseThrow(() -> new BadRequestException(
                        "No fee rule found for amount " + amount
                        + " in configuration " + feeConfigurationId));
    }

    @Override
    public void deleteRule(String ruleId) {
        FeeRule rule = getRuleById(ruleId);
        feeRuleRepository.delete(rule);
    }

    @Override
    public void deleteAllRulesForConfiguration(String feeConfigurationId) {
        feeRuleRepository.deleteAllByFeeConfigurationId(feeConfigurationId);
    }
    
    @Override
    public List<FeeRule> replaceRules(String feeConfigurationId, List<FeeRule> newRules,
                                      String updatedBy) {
        feeConfigurationService.getConfigurationById(feeConfigurationId);
        validateNoBandOverlaps(newRules);

        feeRuleRepository.deleteAllByFeeConfigurationId(feeConfigurationId);

        newRules.forEach(rule -> {
            rule.setFeeConfigurationId(feeConfigurationId);
            rule.setCreatedBy(updatedBy);
        });

        return feeRuleRepository.saveAll(newRules);
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private void validateBand(String configId, FeeRule rule, String excludeRuleId) {
        if (rule.getMaxAmount() != null
                && rule.getMinAmount().compareTo(rule.getMaxAmount()) >= 0) {
            throw new BadRequestException("minAmount must be less than maxAmount");
        }

        List<FeeRule> existing = feeRuleRepository
                .findAllByFeeConfigurationIdOrderByMinAmountAsc(configId);

        for (FeeRule r : existing) {
            if (r.getId().equals(excludeRuleId)) continue;

            boolean overlaps = rule.getMinAmount().compareTo(
                    r.getMaxAmount() != null ? r.getMaxAmount() : BigDecimal.valueOf(Long.MAX_VALUE)) < 0
                    && (rule.getMaxAmount() == null
                    || rule.getMaxAmount().compareTo(r.getMinAmount()) > 0);

            if (overlaps) {
                throw new ConflictException("Fee rule band overlaps with existing rule "
                        + r.getId() + " [" + r.getMinAmount() + " – " + r.getMaxAmount() + "]");
            }
        }
    }

    private void validateNoBandOverlaps(List<FeeRule> rules) {
        for (int i = 0; i < rules.size(); i++) {
            for (int j = i + 1; j < rules.size(); j++) {
                FeeRule a = rules.get(i);
                FeeRule b = rules.get(j);
                boolean overlaps = a.getMinAmount().compareTo(
                        b.getMaxAmount() != null ? b.getMaxAmount() : BigDecimal.valueOf(Long.MAX_VALUE)) < 0
                        && (a.getMaxAmount() == null
                        || a.getMaxAmount().compareTo(b.getMinAmount()) > 0);
                if (overlaps) {
                    throw new ConflictException("Rules at index " + i + " and " + j
                            + " have overlapping amount bands");
                }
            }
        }
    }
}
