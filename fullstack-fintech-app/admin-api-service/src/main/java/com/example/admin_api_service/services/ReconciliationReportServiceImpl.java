package com.example.admin_api_service.services;

import com.example.admin_api_service.Interfaces.IReconciliationExceptionService;
import com.example.admin_api_service.Interfaces.IReconciliationReportService;
import com.example.admin_api_service.enums.ReconciliationExceptionType;
import com.example.admin_api_service.enums.ReconciliationReportStatus;
import com.example.admin_api_service.enums.ReconciliationReportType;
import com.example.admin_api_service.exceptions.ConflictException;
import com.example.admin_api_service.exceptions.ResourceNotFoundException;
import com.example.admin_api_service.models.settlementsAndReconciliation.ReconciliationReport;
import com.example.admin_api_service.repository.ReconciliationReportRepository;
import com.example.admin_api_service.repository.SettlementRecordRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Transactional
public class ReconciliationReportServiceImpl implements IReconciliationReportService {

    private final ReconciliationReportRepository reportRepository;
    private final SettlementRecordRepository settlementRecordRepository;
    private final IReconciliationExceptionService exceptionService;
    private static final AtomicLong reportCounter = new AtomicLong();

    public ReconciliationReportServiceImpl(ReconciliationReportRepository reportRepository,
                                            SettlementRecordRepository settlementRecordRepository,
                                            @Lazy IReconciliationExceptionService exceptionService) {
        this.reportRepository = reportRepository;
        this.settlementRecordRepository = settlementRecordRepository;
        this.exceptionService = exceptionService;
    }

    
    @Override
    public ReconciliationReport createReport(ReconciliationReportType reportType,
                                             String settlementBatchId,
                                             LocalDate reconciliationDate,
                                             LocalDateTime periodStart, LocalDateTime periodEnd,
                                             String currency, String reconciledBy) {
        ReconciliationReport report = new ReconciliationReport();
        report.setReportReference(generateReportReference(reconciliationDate));
        report.setReportType(reportType);
        report.setSettlementBatchId(settlementBatchId);
        report.setReconciliationDate(reconciliationDate);
        report.setPeriodStart(periodStart);
        report.setPeriodEnd(periodEnd);
        report.setCurrency(currency);
        report.setReconciledBy(reconciledBy);
        report.setStatus(ReconciliationReportStatus.PENDING);
        report.setInternalTotalCredits(BigDecimal.ZERO);
        report.setInternalTotalDebits(BigDecimal.ZERO);
        return reportRepository.save(report);
    }

    @Override
    @Transactional(readOnly = true)
    public ReconciliationReport getReportById(String reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("ReconciliationReport", "id", reportId));
    }

    @Override
    @Transactional(readOnly = true)
    public ReconciliationReport getReportByReference(String reportReference) {
        return reportRepository.findByReportReference(reportReference)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ReconciliationReport", "reportReference", reportReference));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReconciliationReport> getAllReports(Pageable pageable) {
        return reportRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReconciliationReport> getReportsByStatus(ReconciliationReportStatus status, Pageable pageable) {
        return reportRepository.findAllByStatus(status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReconciliationReport> getReportsByType(ReconciliationReportType type, Pageable pageable) {
        return reportRepository.findAllByReportType(type, pageable);
    }

    @Override
    public ReconciliationReport populateInternalTotals(String reportId) {
        ReconciliationReport report = getReportById(reportId);

        BigDecimal[] totals = settlementRecordRepository.sumTotalsByCurrencyAndPeriod(
                report.getCurrency(), report.getPeriodStart(), report.getPeriodEnd());

        report.setInternalTotalCredits(totals[0] != null ? totals[0] : BigDecimal.ZERO);
        report.setInternalTotalDebits(totals[1] != null ? totals[1] : BigDecimal.ZERO);
        report.setInternalTransactionCount(
                settlementRecordRepository.countByCurrencyAndPeriod(
                        report.getCurrency(), report.getPeriodStart(), report.getPeriodEnd()));
        report.setStatus(ReconciliationReportStatus.IN_PROGRESS);
        report.setUpdatedOn(LocalDateTime.now());
        return reportRepository.save(report);
    }

    @Override
    public ReconciliationReport loadExternalTotals(String reportId,
                                                   BigDecimal externalCredits,
                                                   BigDecimal externalDebits,
                                                   int externalTransactionCount,
                                                   String externalStatementUrl) {
        ReconciliationReport report = getReportById(reportId);
        report.setExternalTotalCredits(externalCredits);
        report.setExternalTotalDebits(externalDebits);
        report.setExternalTransactionCount(externalTransactionCount);
        report.setExternalStatementUrl(externalStatementUrl);
        report.setUpdatedOn(LocalDateTime.now());
        return reportRepository.save(report);
    }

    @Override
    public ReconciliationReport runReconciliation(String reportId, String reconciledBy) {
        ReconciliationReport report = getReportById(reportId);

        if (report.getExternalTotalCredits() == null) {
            throw new ConflictException("External totals must be loaded before running reconciliation");
        }

        // Compute variances
        BigDecimal creditVariance = report.getInternalTotalCredits()
                .subtract(report.getExternalTotalCredits());
        BigDecimal debitVariance = report.getInternalTotalDebits()
                .subtract(report.getExternalTotalDebits() != null
                        ? report.getExternalTotalDebits() : BigDecimal.ZERO);

        report.setCreditVariance(creditVariance);
        report.setDebitVariance(debitVariance);

        int exceptionCount = 0;

        // Raise credit variance exception if non-zero
        if (creditVariance.compareTo(BigDecimal.ZERO) != 0) {
            exceptionService.createException(
                    reportId,
                    creditVariance.compareTo(BigDecimal.ZERO) > 0
                            ? ReconciliationExceptionType.MISSING_EXTERNALLY
                            : ReconciliationExceptionType.MISSING_INTERNALLY,
                    null, null,
                    report.getCurrency(),
                    report.getInternalTotalCredits(),
                    report.getExternalTotalCredits(),
                    creditVariance.abs(),
                    "Credit variance detected: internal=" + report.getInternalTotalCredits()
                            + " external=" + report.getExternalTotalCredits()
            );
            exceptionCount++;
        }

        // Raise debit variance exception if non-zero
        if (debitVariance.compareTo(BigDecimal.ZERO) != 0) {
            exceptionService.createException(
                    reportId,
                    ReconciliationExceptionType.AMOUNT_MISMATCH,
                    null, null,
                    report.getCurrency(),
                    report.getInternalTotalDebits(),
                    report.getExternalTotalDebits(),
                    debitVariance.abs(),
                    "Debit variance detected: internal=" + report.getInternalTotalDebits()
                            + " external=" + report.getExternalTotalDebits()
            );
            exceptionCount++;
        }

        int internalCount = report.getInternalTransactionCount();
        int externalCount = report.getExternalTransactionCount() != null
                ? report.getExternalTransactionCount() : 0;
        int matched = Math.min(internalCount, externalCount);

        report.setMatchedCount(matched);
        report.setUnmatchedCount(Math.abs(internalCount - externalCount));
        report.setExceptionCount(exceptionCount);
        report.setReconciledBy(reconciledBy);
        report.setReconciledAt(LocalDateTime.now());
        report.setStatus(exceptionCount > 0
                ? ReconciliationReportStatus.COMPLETED_WITH_EXCEPTIONS
                : ReconciliationReportStatus.COMPLETED);
        report.setUpdatedOn(LocalDateTime.now());
        return reportRepository.save(report);
    }

    @Override
    public ReconciliationReport reviewReport(String reportId, String reviewedBy, String reviewNote) {
        ReconciliationReport report = getReportById(reportId);
        report.setReviewedBy(reviewedBy);
        report.setReviewedAt(LocalDateTime.now());
        report.setReviewNote(reviewNote);
        report.setStatus(ReconciliationReportStatus.REVIEWED);
        report.setUpdatedOn(LocalDateTime.now());
        return reportRepository.save(report);
    }

    @Override
    public ReconciliationReport closeReport(String reportId, String closedBy) {
        ReconciliationReport report = getReportById(reportId);
        report.setStatus(ReconciliationReportStatus.CLOSED);
        report.setUpdatedOn(LocalDateTime.now());
        return reportRepository.save(report);
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private String generateReportReference(LocalDate date) {
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long seq = reportRepository.countByReconciliationDate(date) + reportCounter.incrementAndGet();
        return String.format("REC-%s-%03d", dateStr, seq);
    }
}
