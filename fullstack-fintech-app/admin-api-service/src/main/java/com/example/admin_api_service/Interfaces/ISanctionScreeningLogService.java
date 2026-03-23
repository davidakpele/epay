package com.example.admin_api_service.Interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.admin_api_service.enums.SanctionScreeningResult;
import com.example.admin_api_service.enums.SanctionScreeningTrigger;
import com.example.admin_api_service.models.complianceAndRisk.SanctionScreeningLog;

public interface ISanctionScreeningLogService {
    SanctionScreeningLog screen(Long userId, Long walletId, String transactionId,
                                SanctionScreeningTrigger trigger, String screenedName);
 
    SanctionScreeningLog getLogById(String logId);
 
    Page<SanctionScreeningLog> getAllLogs(Pageable pageable);
 
    Page<SanctionScreeningLog> getLogsByUser(Long userId, Pageable pageable);
 
    Page<SanctionScreeningLog> getLogsByResult(SanctionScreeningResult result, Pageable pageable);
 
    Page<SanctionScreeningLog> getLogsByTransaction(String transactionId, Pageable pageable);
 
    SanctionScreeningLog reviewLog(String logId, String reviewedBy, String reviewNote,
                                   SanctionScreeningResult overrideResult);
 
    // Escalate a potential match to an AML case
    SanctionScreeningLog escalateToAmlCase(String logId, String escalatedBy);
}
