package com.example.admin_api_service.Interfaces;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.admin_api_service.enums.ReconciliationReportStatus;
import com.example.admin_api_service.enums.ReconciliationReportType;
import com.example.admin_api_service.models.settlementsAndReconciliation.ReconciliationReport;

public interface IReconciliationReportService {
    ReconciliationReport createReport(ReconciliationReportType reportType,
                                      String settlementBatchId,
                                      LocalDate reconciliationDate,
                                      LocalDateTime periodStart, LocalDateTime periodEnd,
                                      String currency, String reconciledBy);
 
    ReconciliationReport getReportById(String reportId);
 
    ReconciliationReport getReportByReference(String reportReference);
 
    Page<ReconciliationReport> getAllReports(Pageable pageable);
 
    Page<ReconciliationReport> getReportsByStatus(ReconciliationReportStatus status, Pageable pageable);
 
    Page<ReconciliationReport> getReportsByType(ReconciliationReportType type, Pageable pageable);
 
    // Populate internal ledger totals from the DB
    ReconciliationReport populateInternalTotals(String reportId);
 
    // Load external statement figures (from uploaded bank file)
    ReconciliationReport loadExternalTotals(String reportId,
                                            BigDecimal externalCredits, BigDecimal externalDebits,
                                            int externalTransactionCount,
                                            String externalStatementUrl);
 
    // Run matching — computes variances, matched/unmatched counts, and creates exceptions
    ReconciliationReport runReconciliation(String reportId, String reconciledBy);
 
    ReconciliationReport reviewReport(String reportId, String reviewedBy, String reviewNote);
 
    ReconciliationReport closeReport(String reportId, String closedBy);
}
