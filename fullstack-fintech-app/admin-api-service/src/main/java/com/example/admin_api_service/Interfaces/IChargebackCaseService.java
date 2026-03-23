package com.example.admin_api_service.Interfaces;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.admin_api_service.enums.ChargebackCaseStatus;
import com.example.admin_api_service.enums.ChargebackOutcome;
import com.example.admin_api_service.enums.ChargebackReason;
import com.example.admin_api_service.models.settlementsAndReconciliation.ChargebackCase;

public interface IChargebackCaseService {
    ChargebackCase raiseCase(String transactionId, Long walletId, Long userId,
                             ChargebackReason reason, String currency,
                             BigDecimal disputedAmount, String description,
                             String raisedBy);
 
    ChargebackCase getCaseById(String caseId);
 
    ChargebackCase getCaseByCaseReference(String caseReference);
 
    Page<ChargebackCase> getAllCases(Pageable pageable);
 
    Page<ChargebackCase> getCasesByStatus(ChargebackCaseStatus status, Pageable pageable);
 
    Page<ChargebackCase> getCasesByUser(Long userId, Pageable pageable);
 
    Page<ChargebackCase> getCasesByWallet(Long walletId, Pageable pageable);
 
    ChargebackCase assignCase(String caseId, String assignedTo);
 
    ChargebackCase submitEvidence(String caseId, String evidenceUrlsJson, String submittedBy);
 
    ChargebackCase filePreArbitration(String caseId, LocalDate preArbitrationDate, String filedBy);
 
    ChargebackCase fileArbitration(String caseId, LocalDate arbitrationDate, String filedBy);
 
    ChargebackCase resolveCase(String caseId, ChargebackOutcome outcome,
                               BigDecimal recoveredAmount, String resolvedBy, String resolutionNote);
 
    ChargebackCase cancelCase(String caseId, String cancelledBy, String reason);
 
    ChargebackCase linkWalletFreeze(String caseId, String freezeId);
 
    ChargebackCase linkReversalAdjustment(String caseId, String adjustmentId);
 
    ChargebackCase updateNetworkReference(String caseId, String networkReference,
                                          String acquirerReference);
}
