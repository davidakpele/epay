package com.example.admin_api_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import com.example.admin_api_service.enums.RiskRuleStatus;
import com.example.admin_api_service.enums.RiskRuleType;
import com.example.admin_api_service.models.complianceAndRisk.RiskRule;

@Repository
public interface RiskRuleRepository extends JpaRepository<RiskRule, String> {
    Optional<RiskRule> findByCode(String code);
    boolean existsByCode(String code);
    List<RiskRule> findAllByStatusInOrderByPriorityDesc(List<RiskRuleStatus> statuses);
    List<RiskRule> findAllByRuleTypeAndStatusIn(RiskRuleType ruleType, List<RiskRuleStatus> statuses);
}
