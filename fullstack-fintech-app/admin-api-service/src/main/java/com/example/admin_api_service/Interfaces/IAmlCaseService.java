package com.example.admin_api_service.Interfaces;

import com.example.admin_api_service.enums.AmlCasePriority;
import com.example.admin_api_service.enums.AmlCaseStatus;
import com.example.admin_api_service.enums.AmlCaseType;
import com.example.admin_api_service.models.complianceAndRisk.AmlCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IAmlCaseService {
    AmlCase openCase(Long userId, Long walletId, AmlCaseType caseType,
                     AmlCasePriority priority, String title, String description,
                     String openedBy);
 
    AmlCase getCaseById(String caseId);
 
    AmlCase getCaseByCaseReference(String caseReference);
 
    Page<AmlCase> getAllCases(Pageable pageable);
 
    Page<AmlCase> getCasesByStatus(AmlCaseStatus status, Pageable pageable);
 
    Page<AmlCase> getCasesByPriority(AmlCasePriority priority, Pageable pageable);
 
    Page<AmlCase> getCasesByUser(Long userId, Pageable pageable);
 
    AmlCase assignCase(String caseId, String assignedTo);
 
    AmlCase escalateCase(String caseId, String escalatedTo, String escalationReason);
 
    AmlCase fileSar(String caseId, String sarReference, String sarFiledBy);
 
    AmlCase resolveCase(String caseId, boolean suspiciousActivityConfirmed,
                        String closedBy, String closureNote);
 
    AmlCase closeCase(String caseId, String closedBy, String closureNote);
 
    AmlCase linkFreezeToCase(String caseId, String freezeId);
 
    AmlCase updateActivitySummary(String caseId, String activitySummaryJson);
}
