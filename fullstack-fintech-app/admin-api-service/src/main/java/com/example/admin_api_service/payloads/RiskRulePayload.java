package com.example.admin_api_service.payloads;

import com.example.admin_api_service.enums.RiskRuleAction;
import com.example.admin_api_service.enums.RiskRuleType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskRulePayload {
    @NotBlank(message = "Rule code is required")
    @Size(max = 50)
    private String code;
 
    @NotBlank(message = "Rule name is required")
    @Size(max = 100)
    private String name;
 
    @Size(max = 500)
    private String description;
 
    @NotNull(message = "Rule type is required")
    private RiskRuleType ruleType;
 
    @NotNull(message = "Action is required")
    private RiskRuleAction action;
 
    @Min(1) @Max(100)
    private int scoreWeight = 50;
 
    private String conditions;
 
    private String applicableTiers;
 
    private String applicableChannels;
 
    private boolean blocking = false;
 
    private Integer cooldownMinutes;
 
    @Min(1) @Max(1000)
    private int priority = 100;
 
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public RiskRuleType getRuleType() { return ruleType; }
    public void setRuleType(RiskRuleType ruleType) { this.ruleType = ruleType; }
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
    public boolean isBlocking() { return blocking; }
    public void setBlocking(boolean blocking) { this.blocking = blocking; }
    public Integer getCooldownMinutes() { return cooldownMinutes; }
    public void setCooldownMinutes(Integer cooldownMinutes) { this.cooldownMinutes = cooldownMinutes; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
}
