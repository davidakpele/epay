package com.example.admin_api_service.models.complianceAndRisk;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.admin_api_service.enums.RiskRuleEvaluationOutcome;
import com.example.admin_api_service.models.userAndWalletManagement.AccountFlag;

@Entity
@Table(name = "risk_rule_evaluations")
public class RiskRuleEvaluation {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id = UUID.randomUUID().toString();

    @Column(name = "risk_rule_id", length = 36, nullable = false)
    private String riskRuleId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "wallet_id")
    private Long walletId;

    // Transaction evaluated (null for periodic/batch evaluations)
    @Column(name = "transaction_id", length = 36)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", length = 20, nullable = false)
    private RiskRuleEvaluationOutcome outcome;

    // Score contributed by this evaluation
    @Column(name = "score_contribution", precision = 5, scale = 2, nullable = false)
    private BigDecimal scoreContribution = BigDecimal.ZERO;

    // Input values that were checked stored as JSON
    @Column(name = "input_context", columnDefinition = "json")
    private String inputContext;

    // Computed values during evaluation stored as JSON
    @Column(name = "evaluation_details", columnDefinition = "json")
    private String evaluationDetails;

    // AML alert created as a result of this evaluation (if any)
    @Column(name = "triggered_alert_id", length = 36)
    private String triggeredAlertId;

    // Account flag raised as a result of this evaluation (if any)
    @Column(name = "triggered_flag_id", length = 36)
    private String triggeredFlagId;

    // Whether the transaction was blocked by this rule
    @Column(name = "transaction_blocked", nullable = false)
    private boolean transactionBlocked = false;

    @Column(name = "evaluated_at", nullable = false)
    private LocalDateTime evaluatedAt = LocalDateTime.now();

    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn = LocalDateTime.now();

    // Navigation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "risk_rule_id", insertable = false, updatable = false)
    private RiskRule riskRule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "triggered_alert_id", insertable = false, updatable = false)
    private AmlAlert triggeredAlert;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "triggered_flag_id", insertable = false, updatable = false)
    private AccountFlag triggeredFlag;

    public RiskRuleEvaluation() {
    }

    public RiskRuleEvaluation(String id, String riskRuleId, Long userId, Long walletId, String transactionId, RiskRuleEvaluationOutcome outcome, BigDecimal scoreContribution, String inputContext, String evaluationDetails, String triggeredAlertId, String triggeredFlagId, boolean transactionBlocked, LocalDateTime evaluatedAt, LocalDateTime createdOn, RiskRule riskRule, AmlAlert triggeredAlert, AccountFlag triggeredFlag) {
        this.id = id;
        this.riskRuleId = riskRuleId;
        this.userId = userId;
        this.walletId = walletId;
        this.transactionId = transactionId;
        this.outcome = outcome;
        this.scoreContribution = scoreContribution;
        this.inputContext = inputContext;
        this.evaluationDetails = evaluationDetails;
        this.triggeredAlertId = triggeredAlertId;
        this.triggeredFlagId = triggeredFlagId;
        this.transactionBlocked = transactionBlocked;
        this.evaluatedAt = evaluatedAt;
        this.createdOn = createdOn;
        this.riskRule = riskRule;
        this.triggeredAlert = triggeredAlert;
        this.triggeredFlag = triggeredFlag;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRiskRuleId() { return riskRuleId; }
    public void setRiskRuleId(String riskRuleId) { this.riskRuleId = riskRuleId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getWalletId() { return walletId; }
    public void setWalletId(Long walletId) { this.walletId = walletId; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public RiskRuleEvaluationOutcome getOutcome() { return outcome; }
    public void setOutcome(RiskRuleEvaluationOutcome outcome) { this.outcome = outcome; }

    public BigDecimal getScoreContribution() { return scoreContribution; }
    public void setScoreContribution(BigDecimal scoreContribution) { this.scoreContribution = scoreContribution; }

    public String getInputContext() { return inputContext; }
    public void setInputContext(String inputContext) { this.inputContext = inputContext; }

    public String getEvaluationDetails() { return evaluationDetails; }
    public void setEvaluationDetails(String evaluationDetails) { this.evaluationDetails = evaluationDetails; }

    public String getTriggeredAlertId() { return triggeredAlertId; }
    public void setTriggeredAlertId(String triggeredAlertId) { this.triggeredAlertId = triggeredAlertId; }

    public String getTriggeredFlagId() { return triggeredFlagId; }
    public void setTriggeredFlagId(String triggeredFlagId) { this.triggeredFlagId = triggeredFlagId; }

    public boolean isTransactionBlocked() { return transactionBlocked; }
    public void setTransactionBlocked(boolean transactionBlocked) { this.transactionBlocked = transactionBlocked; }

    public LocalDateTime getEvaluatedAt() { return evaluatedAt; }
    public void setEvaluatedAt(LocalDateTime evaluatedAt) { this.evaluatedAt = evaluatedAt; }

    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }

    public RiskRule getRiskRule() { return riskRule; }
    public void setRiskRule(RiskRule riskRule) { this.riskRule = riskRule; }

    public AmlAlert getTriggeredAlert() { return triggeredAlert; }
    public void setTriggeredAlert(AmlAlert triggeredAlert) { this.triggeredAlert = triggeredAlert; }

    public AccountFlag getTriggeredFlag() { return triggeredFlag; }
    public void setTriggeredFlag(AccountFlag triggeredFlag) { this.triggeredFlag = triggeredFlag; }
}
