package com.example.admin_api_service.Interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.List;
import com.example.admin_api_service.enums.ReconciliationExceptionStatus;
import com.example.admin_api_service.enums.ReconciliationExceptionType;
import com.example.admin_api_service.models.settlementsAndReconciliation.ReconciliationException;

public interface IReconciliationExceptionService {
    ReconciliationException createException(String reconciliationReportId,
                                            ReconciliationExceptionType exceptionType,
                                            String internalTransactionId,
                                            String externalReference,
                                            String currency,
                                            BigDecimal internalAmount,
                                            BigDecimal externalAmount,
                                            BigDecimal varianceAmount,
                                            String description);
 
    ReconciliationException getExceptionById(String exceptionId);
 
    Page<ReconciliationException> getAllExceptions(Pageable pageable);
 
    Page<ReconciliationException> getExceptionsByReport(String reportId, Pageable pageable);
 
    Page<ReconciliationException> getExceptionsByStatus(ReconciliationExceptionStatus status,
                                                        Pageable pageable);
 
    List<ReconciliationException> getOpenExceptionsForReport(String reportId);
 
    ReconciliationException assignException(String exceptionId, String assignedTo);
 
    ReconciliationException resolveException(String exceptionId, String resolvedBy,
                                             String resolutionNote);
 
    ReconciliationException writeOffException(String exceptionId, String resolvedBy,
                                              String resolutionNote);
 
    ReconciliationException escalateToChargeback(String exceptionId, String escalatedBy);
 
    ReconciliationException linkManualAdjustment(String exceptionId, String manualAdjustmentId);
}
