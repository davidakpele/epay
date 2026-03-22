package com.example.admin_api_service.enums;

public enum RiskRuleEvaluationOutcome {
    PASSED,        // Rule evaluated — no breach
    TRIGGERED,     // Rule fired — action taken
    SKIPPED,       // Rule skipped (cooldown, inactive, or not applicable)
    ERROR          // Rule evaluation failed
}
 