package com.example.admin_api_service.services;

import com.example.admin_api_service.Interfaces.IRiskRuleService;
import com.example.admin_api_service.enums.RiskRuleStatus;
import com.example.admin_api_service.enums.RiskRuleType;
import com.example.admin_api_service.exceptions.ConflictException;
import com.example.admin_api_service.exceptions.ResourceNotFoundException;
import com.example.admin_api_service.models.complianceAndRisk.RiskRule;
import com.example.admin_api_service.repository.RiskRuleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class RiskRuleServiceImpl implements IRiskRuleService {

    private final RiskRuleRepository riskRuleRepository;

    public RiskRuleServiceImpl(RiskRuleRepository riskRuleRepository) {
        this.riskRuleRepository = riskRuleRepository;
    }

    @Override
    public RiskRule createRule(RiskRule rule, String createdBy) {
        if (riskRuleRepository.existsByCode(rule.getCode())) {
            throw new ConflictException("Risk rule with code '" + rule.getCode() + "' already exists");
        }
        rule.setCreatedBy(createdBy);
        rule.setStatus(RiskRuleStatus.ACTIVE);
        return riskRuleRepository.save(rule);
    }

    @Override
    public RiskRule updateRule(String ruleId, RiskRule updated, String updatedBy) {
        RiskRule existing = getRuleById(ruleId);

        if (!existing.getCode().equals(updated.getCode())
                && riskRuleRepository.existsByCode(updated.getCode())) {
            throw new ConflictException("Risk rule with code '" + updated.getCode() + "' already exists");
        }

        existing.setCode(updated.getCode());
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setRuleType(updated.getRuleType());
        existing.setAction(updated.getAction());
        existing.setScoreWeight(updated.getScoreWeight());
        existing.setConditions(updated.getConditions());
        existing.setApplicableTiers(updated.getApplicableTiers());
        existing.setApplicableChannels(updated.getApplicableChannels());
        existing.setBlocking(updated.isBlocking());
        existing.setCooldownMinutes(updated.getCooldownMinutes());
        existing.setPriority(updated.getPriority());
        existing.setUpdatedBy(updatedBy);
        existing.setUpdatedOn(LocalDateTime.now());
        return riskRuleRepository.save(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public RiskRule getRuleById(String ruleId) {
        return riskRuleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("RiskRule", "id", ruleId));
    }

    @Override
    @Transactional(readOnly = true)
    public RiskRule getRuleByCode(String code) {
        return riskRuleRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("RiskRule", "code", code));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RiskRule> getAllRules(Pageable pageable) {
        return riskRuleRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RiskRule> getActiveRules() {
        return riskRuleRepository.findAllByStatusInOrderByPriorityDesc(
                List.of(RiskRuleStatus.ACTIVE, RiskRuleStatus.SHADOW_MODE));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RiskRule> getRulesByType(RiskRuleType ruleType) {
        return riskRuleRepository.findAllByRuleTypeAndStatusIn(
                ruleType, List.of(RiskRuleStatus.ACTIVE, RiskRuleStatus.SHADOW_MODE));
    }

    @Override
    public void activateRule(String ruleId, String updatedBy) {
        RiskRule rule = getRuleById(ruleId);
        rule.setStatus(RiskRuleStatus.ACTIVE);
        rule.setUpdatedBy(updatedBy);
        rule.setUpdatedOn(LocalDateTime.now());
        riskRuleRepository.save(rule);
    }

    @Override
    public void deactivateRule(String ruleId, String updatedBy) {
        RiskRule rule = getRuleById(ruleId);
        rule.setStatus(RiskRuleStatus.INACTIVE);
        rule.setUpdatedBy(updatedBy);
        rule.setUpdatedOn(LocalDateTime.now());
        riskRuleRepository.save(rule);
    }

    @Override
    public void setRuleToShadowMode(String ruleId, String updatedBy) {
        RiskRule rule = getRuleById(ruleId);
        rule.setStatus(RiskRuleStatus.SHADOW_MODE);
        rule.setUpdatedBy(updatedBy);
        rule.setUpdatedOn(LocalDateTime.now());
        riskRuleRepository.save(rule);
    }

    @Override
    public void deleteRule(String ruleId) {
        RiskRule rule = getRuleById(ruleId);
        if (rule.getStatus() == RiskRuleStatus.ACTIVE) {
            throw new ConflictException("Active rules cannot be deleted. Deactivate the rule first.");
        }
        riskRuleRepository.delete(rule);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        return riskRuleRepository.existsByCode(code);
    }
}
