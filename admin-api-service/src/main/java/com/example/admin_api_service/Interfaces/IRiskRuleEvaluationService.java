package com.example.admin_api_service.Interfaces;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.admin_api_service.enums.RiskRuleEvaluationOutcome;
import com.example.admin_api_service.models.complianceAndRisk.RiskRuleEvaluation;

public interface IRiskRuleEvaluationService {
    List<RiskRuleEvaluation> evaluateTransaction(Long userId, Long walletId,
                                                  String transactionId,
                                                  String inputContextJson);
 
    // Evaluate a single named rule
    RiskRuleEvaluation evaluateSingleRule(String ruleCode, Long userId, Long walletId,
                                          String transactionId, String inputContextJson);
 
    RiskRuleEvaluation getEvaluationById(String evaluationId);
 
    Page<RiskRuleEvaluation> getEvaluationsByUser(Long userId, Pageable pageable);
 
    Page<RiskRuleEvaluation> getEvaluationsByTransaction(String transactionId, Pageable pageable);
 
    Page<RiskRuleEvaluation> getEvaluationsByRule(String ruleId, Pageable pageable);
 
    Page<RiskRuleEvaluation> getEvaluationsByOutcome(RiskRuleEvaluationOutcome outcome, Pageable pageable);
 
    // Total accumulated risk score for a user across triggered evaluations
    BigDecimal calculateUserRiskScore(Long userId);
}
