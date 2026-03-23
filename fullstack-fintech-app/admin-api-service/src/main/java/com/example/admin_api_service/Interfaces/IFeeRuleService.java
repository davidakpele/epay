package com.example.admin_api_service.Interfaces;

import java.math.BigDecimal;
import java.util.List;
import com.example.admin_api_service.models.feeAndLimits.FeeRule;

public interface IFeeRuleService {
    FeeRule createRule(String feeConfigurationId, FeeRule rule, String createdBy);
 
    FeeRule updateRule(String ruleId, FeeRule updated, String updatedBy);
 
    FeeRule getRuleById(String ruleId);
 
    List<FeeRule> getRulesForConfiguration(String feeConfigurationId);
 
    // Find the matching rule for a given transaction amount (by band)
    FeeRule resolveRuleForAmount(String feeConfigurationId, BigDecimal amount);
 
    void deleteRule(String ruleId);
 
    void deleteAllRulesForConfiguration(String feeConfigurationId);
 
    // Replace all existing rules for a configuration in a single atomic operation
    List<FeeRule> replaceRules(String feeConfigurationId, List<FeeRule> newRules, String updatedBy);
}
