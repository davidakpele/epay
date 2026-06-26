package com.example.admin_api_service.Interfaces;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.admin_api_service.enums.RiskRuleType;
import com.example.admin_api_service.models.complianceAndRisk.RiskRule;

public interface IRiskRuleService {
    RiskRule createRule(RiskRule rule, String createdBy);
 
    RiskRule updateRule(String ruleId, RiskRule updated, String updatedBy);
 
    RiskRule getRuleById(String ruleId);
 
    RiskRule getRuleByCode(String code);
 
    Page<RiskRule> getAllRules(Pageable pageable);
 
    List<RiskRule> getActiveRules();
 
    List<RiskRule> getRulesByType(RiskRuleType ruleType);
 
    void activateRule(String ruleId, String updatedBy);
 
    void deactivateRule(String ruleId, String updatedBy);

    void setRuleToShadowMode(String ruleId, String updatedBy);
 
    void deleteRule(String ruleId);
 
    boolean existsByCode(String code);
}
