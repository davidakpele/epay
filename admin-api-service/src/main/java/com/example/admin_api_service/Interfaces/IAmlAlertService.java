package com.example.admin_api_service.Interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.admin_api_service.enums.AmlAlertSeverity;
import com.example.admin_api_service.enums.AmlAlertStatus;
import com.example.admin_api_service.enums.AmlAlertType;
import com.example.admin_api_service.models.complianceAndRisk.AmlAlert;
import com.example.admin_api_service.models.complianceAndRisk.RiskRule;
import java.math.BigDecimal;
import java.util.List;

public interface IAmlAlertService {
    // Auto-raised by a risk rule evaluation
    AmlAlert raiseAlertFromRule(RiskRule rule, Long userId, Long walletId,
                                String transactionId, String evidence);
 
    // Manually raised by an admin
    AmlAlert raiseManualAlert(Long userId, Long walletId, String transactionId,
                              AmlAlertType alertType, AmlAlertSeverity severity,
                              String description, BigDecimal amount, String currency,
                              String evidence);
 
    AmlAlert getAlertById(String alertId);
 
    Page<AmlAlert> getAllAlerts(Pageable pageable);
 
    Page<AmlAlert> getAlertsByStatus(AmlAlertStatus status, Pageable pageable);
 
    Page<AmlAlert> getAlertsBySeverity(AmlAlertSeverity severity, Pageable pageable);
 
    Page<AmlAlert> getAlertsByUser(Long userId, Pageable pageable);
 
    List<AmlAlert> getOpenAlertsForUser(Long userId);
 
    AmlAlert reviewAlert(String alertId, String reviewedBy, String reviewNote);
 
    // Escalate alert to a full AML case
    AmlAlert escalateToCase(String alertId, String escalatedBy);
 
    AmlAlert markFalsePositive(String alertId, String reviewedBy, String reviewNote);
 
    AmlAlert closeAlert(String alertId, String closedBy, String note);
}
