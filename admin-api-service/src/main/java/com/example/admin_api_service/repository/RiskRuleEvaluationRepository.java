package com.example.admin_api_service.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.admin_api_service.enums.RiskRuleEvaluationOutcome;
import com.example.admin_api_service.models.complianceAndRisk.RiskRuleEvaluation;

@Repository
public interface RiskRuleEvaluationRepository extends JpaRepository<RiskRuleEvaluation, String> {
    Page<RiskRuleEvaluation> findAllByUserId(Long userId, Pageable pageable);
    Page<RiskRuleEvaluation> findAllByTransactionId(String transactionId, Pageable pageable);
    Page<RiskRuleEvaluation> findAllByRiskRuleId(String riskRuleId, Pageable pageable);
    Page<RiskRuleEvaluation> findAllByOutcome(RiskRuleEvaluationOutcome outcome, Pageable pageable);
    boolean existsByRiskRuleIdAndUserIdAndOutcomeAndEvaluatedAtAfter(
            String riskRuleId, Long userId,
            RiskRuleEvaluationOutcome outcome, LocalDateTime after);
 
    @Query("SELECT SUM(e.scoreContribution) FROM RiskRuleEvaluation e " +
           "WHERE e.userId = :userId AND e.outcome = 'TRIGGERED'")
    Optional<BigDecimal> sumScoreContributionByUserId(@Param("userId") Long userId);
}
