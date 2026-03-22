package com.example.admin_api_service.models.complianceAndRisk;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.admin_api_service.enums.RiskRuleAction;
import com.example.admin_api_service.enums.RiskRuleStatus;
import com.example.admin_api_service.enums.RiskRuleType;

@Entity
@Table(name = "risk_rules")
public class RiskRule {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id = UUID.randomUUID().toString();

    // Unique machine-readable code e.g. "AML_VELOCITY_DAILY_LIMIT"
    @Column(name = "code", length = 100, nullable = false, unique = true)
    private String code;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", length = 50, nullable = false)
    private RiskRuleType ruleType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private RiskRuleStatus status = RiskRuleStatus.ACTIVE;

    // Action to take when this rule fires
    @Enumerated(EnumType.STRING)
    @Column(name = "action", length = 30, nullable = false)
    private RiskRuleAction action;

    // Risk score added to user score when this rule fires (0–100)
    @Column(name = "score_weight", nullable = false)
    private int scoreWeight = 0;

    @Column(name = "conditions", columnDefinition = "json", nullable = false)
    private String conditions;

    // Whether this rule applies only to specific KYC tiers stored as JSON array
    @Column(name = "applicable_tiers", columnDefinition = "json")
    private String applicableTiers;

    // Whether this rule applies only to specific transaction channels stored as JSON array
    @Column(name = "applicable_channels", columnDefinition = "json")
    private String applicableChannels;

    @Column(name = "is_blocking", nullable = false)
    private boolean isBlocking = false;

    // Cooldown period before the same rule can re-fire for the same user (minutes)
    @Column(name = "cooldown_minutes")
    private Integer cooldownMinutes;

    @Column(name = "priority", nullable = false)
    private int priority = 0;

    @Column(name = "created_by", length = 36)
    private String createdBy;

    @Column(name = "updated_by", length = 36)
    private String updatedBy;

    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn = LocalDateTime.now();

    @Column(name = "updated_on", nullable = false)
    private LocalDateTime updatedOn = LocalDateTime.now();

    // Navigation
    @OneToMany(mappedBy = "riskRule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RiskRuleEvaluation> evaluations = new ArrayList<>();

    @PreUpdate
    public void preUpdate() {
        this.updatedOn = LocalDateTime.now();
    }

    public RiskRule() {
    }

    public RiskRule(String id, String code, String name, String description, RiskRuleType ruleType, RiskRuleStatus status, RiskRuleAction action, int scoreWeight, String conditions, String applicableTiers, String applicableChannels, boolean isBlocking, Integer cooldownMinutes, int priority, String createdBy, String updatedBy, LocalDateTime createdOn, LocalDateTime updatedOn, List<RiskRuleEvaluation> evaluations) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.ruleType = ruleType;
        this.status = status;
        this.action = action;
        this.scoreWeight = scoreWeight;
        this.conditions = conditions;
        this.applicableTiers = applicableTiers;
        this.applicableChannels = applicableChannels;
        this.isBlocking = isBlocking;
        this.cooldownMinutes = cooldownMinutes;
        this.priority = priority;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
        this.evaluations = evaluations;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public RiskRuleType getRuleType() { return ruleType; }
    public void setRuleType(RiskRuleType ruleType) { this.ruleType = ruleType; }

    public RiskRuleStatus getStatus() { return status; }
    public void setStatus(RiskRuleStatus status) { this.status = status; }

    public RiskRuleAction getAction() { return action; }
    public void setAction(RiskRuleAction action) { this.action = action; }

    public int getScoreWeight() { return scoreWeight; }
    public void setScoreWeight(int scoreWeight) { this.scoreWeight = scoreWeight; }

    public String getConditions() { return conditions; }
    public void setConditions(String conditions) { this.conditions = conditions; }

    public String getApplicableTiers() { return applicableTiers; }
    public void setApplicableTiers(String applicableTiers) { this.applicableTiers = applicableTiers; }

    public String getApplicableChannels() { return applicableChannels; }
    public void setApplicableChannels(String applicableChannels) { this.applicableChannels = applicableChannels; }

    public boolean isBlocking() { return isBlocking; }
    public void setBlocking(boolean blocking) { isBlocking = blocking; }

    public Integer getCooldownMinutes() { return cooldownMinutes; }
    public void setCooldownMinutes(Integer cooldownMinutes) { this.cooldownMinutes = cooldownMinutes; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }

    public LocalDateTime getUpdatedOn() { return updatedOn; }
    public void setUpdatedOn(LocalDateTime updatedOn) { this.updatedOn = updatedOn; }

    public List<RiskRuleEvaluation> getEvaluations() { return evaluations; }
    public void setEvaluations(List<RiskRuleEvaluation> evaluations) { this.evaluations = evaluations; }
}
