package com.example.admin_api_service.services;

import com.example.admin_api_service.Interfaces.IAccountFlagService;
import com.example.admin_api_service.Interfaces.IAmlAlertService;
import com.example.admin_api_service.Interfaces.IRiskRuleEvaluationService;
import com.example.admin_api_service.Interfaces.IRiskRuleService;
import com.example.admin_api_service.enums.AccountFlagSeverity;
import com.example.admin_api_service.enums.AccountFlagType;
import com.example.admin_api_service.enums.RiskRuleAction;
import com.example.admin_api_service.enums.RiskRuleEvaluationOutcome;
import com.example.admin_api_service.enums.RiskRuleStatus;
import com.example.admin_api_service.models.complianceAndRisk.RiskRule;
import com.example.admin_api_service.models.complianceAndRisk.RiskRuleEvaluation;
import com.example.admin_api_service.models.userAndWalletManagement.AccountFlag;
import com.example.admin_api_service.repository.RiskRuleEvaluationRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class RiskRuleEvaluationServiceImpl implements IRiskRuleEvaluationService {

    private final RiskRuleEvaluationRepository evaluationRepository;
    private final IRiskRuleService riskRuleService;
    private final IAmlAlertService amlAlertService;
    private final IAccountFlagService accountFlagService;

    public RiskRuleEvaluationServiceImpl(RiskRuleEvaluationRepository evaluationRepository,
                                          IRiskRuleService riskRuleService,
                                          @Lazy IAmlAlertService amlAlertService,
                                          @Lazy IAccountFlagService accountFlagService) {
        this.evaluationRepository = evaluationRepository;
        this.riskRuleService = riskRuleService;
        this.amlAlertService = amlAlertService;
        this.accountFlagService = accountFlagService;
    }

    @Override
    public List<RiskRuleEvaluation> evaluateTransaction(Long userId, Long walletId,
                                                         String transactionId,
                                                         String inputContextJson) {
        List<RiskRule> activeRules = riskRuleService.getActiveRules();
        List<RiskRuleEvaluation> results = new ArrayList<>();

        for (RiskRule rule : activeRules) {
            // Skip rules in cooldown for this user
            if (isRuleInCooldown(rule, userId)) {
                RiskRuleEvaluation skipped = buildEvaluation(rule, userId, walletId,
                        transactionId, inputContextJson);
                skipped.setOutcome(RiskRuleEvaluationOutcome.SKIPPED);
                results.add(evaluationRepository.save(skipped));
                continue;
            }

            RiskRuleEvaluation evaluation = buildEvaluation(rule, userId, walletId,
                    transactionId, inputContextJson);

            boolean triggered = checkRuleConditions(rule, inputContextJson);

            if (triggered) {
                evaluation.setOutcome(RiskRuleEvaluationOutcome.TRIGGERED);
                evaluation.setScoreContribution(BigDecimal.valueOf(rule.getScoreWeight()));

                // Only execute actions for fully ACTIVE rules — not SHADOW_MODE
                if (rule.getStatus() == RiskRuleStatus.ACTIVE) {
                    executeRuleAction(rule, evaluation, userId, walletId, transactionId);
                }
            } else {
                evaluation.setOutcome(RiskRuleEvaluationOutcome.PASSED);
                evaluation.setScoreContribution(BigDecimal.ZERO);
            }

            results.add(evaluationRepository.save(evaluation));
        }

        return results;
    }

    @Override
    public RiskRuleEvaluation evaluateSingleRule(String ruleCode, Long userId, Long walletId,
                                                  String transactionId, String inputContextJson) {
        RiskRule rule = riskRuleService.getRuleByCode(ruleCode);
        RiskRuleEvaluation evaluation = buildEvaluation(rule, userId, walletId,
                transactionId, inputContextJson);

        boolean triggered = checkRuleConditions(rule, inputContextJson);

        if (triggered) {
            evaluation.setOutcome(RiskRuleEvaluationOutcome.TRIGGERED);
            evaluation.setScoreContribution(BigDecimal.valueOf(rule.getScoreWeight()));
            if (rule.getStatus() == RiskRuleStatus.ACTIVE) {
                executeRuleAction(rule, evaluation, userId, walletId, transactionId);
            }
        } else {
            evaluation.setOutcome(RiskRuleEvaluationOutcome.PASSED);
            evaluation.setScoreContribution(BigDecimal.ZERO);
        }

        return evaluationRepository.save(evaluation);
    }

    @Override
    @Transactional(readOnly = true)
    public RiskRuleEvaluation getEvaluationById(String evaluationId) {
        return evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new com.example.admin_api_service.exceptions.ResourceNotFoundException(
                        "RiskRuleEvaluation", "id", evaluationId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RiskRuleEvaluation> getEvaluationsByUser(Long userId, Pageable pageable) {
        return evaluationRepository.findAllByUserId(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RiskRuleEvaluation> getEvaluationsByTransaction(String transactionId, Pageable pageable) {
        return evaluationRepository.findAllByTransactionId(transactionId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RiskRuleEvaluation> getEvaluationsByRule(String ruleId, Pageable pageable) {
        return evaluationRepository.findAllByRiskRuleId(ruleId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RiskRuleEvaluation> getEvaluationsByOutcome(RiskRuleEvaluationOutcome outcome, Pageable pageable) {
        return evaluationRepository.findAllByOutcome(outcome, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateUserRiskScore(Long userId) {
        return evaluationRepository.sumScoreContributionByUserId(userId)
                .orElse(BigDecimal.ZERO);
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private RiskRuleEvaluation buildEvaluation(RiskRule rule, Long userId, Long walletId,
                                               String transactionId, String inputContextJson) {
        RiskRuleEvaluation evaluation = new RiskRuleEvaluation();
        evaluation.setRiskRuleId(rule.getId());
        evaluation.setUserId(userId);
        evaluation.setWalletId(walletId);
        evaluation.setTransactionId(transactionId);
        evaluation.setInputContext(inputContextJson);
        evaluation.setEvaluatedAt(LocalDateTime.now());
        return evaluation;
    }

    private boolean checkRuleConditions(RiskRule rule, String inputContextJson) {
        // Stub — in production this deserializes rule.getConditions() and
        // evaluates them against the inputContextJson using a rule engine
        // (e.g. Easy Rules, Drools, or custom JSON-based evaluator)
        return false;
    }

    private void executeRuleAction(RiskRule rule, RiskRuleEvaluation evaluation,
                                   Long userId, Long walletId, String transactionId) {
        RiskRuleAction action = rule.getAction();

        switch (action) {
            case RAISE_ALERT -> {
                var alert = amlAlertService.raiseAlertFromRule(
                        rule, userId, walletId, transactionId, evaluation.getInputContext());
                evaluation.setTriggeredAlertId(alert.getId());
            }
            case RAISE_FLAG -> {
                AccountFlag flag = accountFlagService.raiseSystemFlag(
                        userId, walletId,
                        AccountFlagType.FRAUD_SUSPECTED,
                        AccountFlagSeverity.HIGH,
                        "Risk rule triggered: " + rule.getName(),
                        rule.getDescription(),
                        rule.getCode(),
                        evaluation.getInputContext(),
                        null
                );
                evaluation.setTriggeredFlagId(flag.getId());
            }
            case BLOCK_TRANSACTION -> evaluation.setTransactionBlocked(true);
            case SCORE_ONLY -> { /* score already set — no further action */ }
            default -> { /* NOTIFY_COMPLIANCE, REQUIRE_REVIEW, etc. handled by downstream events */ }
        }
    }

    private boolean isRuleInCooldown(RiskRule rule, Long userId) {
        if (rule.getCooldownMinutes() == null) return false;
        LocalDateTime cooldownCutoff = LocalDateTime.now().minusMinutes(rule.getCooldownMinutes());
        return evaluationRepository.existsByRiskRuleIdAndUserIdAndOutcomeAndEvaluatedAtAfter(
                rule.getId(), userId, RiskRuleEvaluationOutcome.TRIGGERED, cooldownCutoff);
    }
}