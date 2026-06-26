package com.example.admin_api_service.models.settlementsAndReconciliation;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.admin_api_service.enums.ReconciliationReportStatus;
import com.example.admin_api_service.enums.ReconciliationReportType;

@Entity
@Table(name = "reconciliation_reports")
public class ReconciliationReport {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id = UUID.randomUUID().toString();

    // Human-readable reference e.g. REC-20240315-001
    @Column(name = "report_reference", length = 50, nullable = false, unique = true)
    private String reportReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", length = 30, nullable = false)
    private ReconciliationReportType reportType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private ReconciliationReportStatus status = ReconciliationReportStatus.PENDING;

    // Linked settlement batch if this report covers one batch
    @Column(name = "settlement_batch_id", length = 36)
    private String settlementBatchId;

    @Column(name = "reconciliation_date", nullable = false)
    private LocalDate reconciliationDate;

    @Column(name = "period_start", nullable = false)
    private LocalDateTime periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDateTime periodEnd;

    @Column(name = "currency", length = 10, nullable = false)
    private String currency;

    // Internal ledger totals
    @Column(name = "internal_total_credits", precision = 18, scale = 4, nullable = false)
    private BigDecimal internalTotalCredits = BigDecimal.ZERO;

    @Column(name = "internal_total_debits", precision = 18, scale = 4, nullable = false)
    private BigDecimal internalTotalDebits = BigDecimal.ZERO;

    @Column(name = "internal_transaction_count", nullable = false)
    private int internalTransactionCount = 0;

    // External (bank / processor statement) totals
    @Column(name = "external_total_credits", precision = 18, scale = 4)
    private BigDecimal externalTotalCredits;

    @Column(name = "external_total_debits", precision = 18, scale = 4)
    private BigDecimal externalTotalDebits;

    @Column(name = "external_transaction_count")
    private Integer externalTransactionCount;

    // Difference between internal and external
    @Column(name = "credit_variance", precision = 18, scale = 4)
    private BigDecimal creditVariance;

    @Column(name = "debit_variance", precision = 18, scale = 4)
    private BigDecimal debitVariance;

    @Column(name = "matched_count", nullable = false)
    private int matchedCount = 0;

    @Column(name = "unmatched_count", nullable = false)
    private int unmatchedCount = 0;

    @Column(name = "exception_count", nullable = false)
    private int exceptionCount = 0;

    // File URL of the external statement used for reconciliation
    @Column(name = "external_statement_url", length = 500)
    private String externalStatementUrl;

    // Generated report file URL
    @Column(name = "report_file_url", length = 500)
    private String reportFileUrl;

    @Column(name = "reconciled_by", length = 36)
    private String reconciledBy;

    @Column(name = "reconciled_at")
    private LocalDateTime reconciledAt;

    @Column(name = "reviewed_by", length = 36)
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "review_note", length = 500)
    private String reviewNote;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn = LocalDateTime.now();

    @Column(name = "updated_on", nullable = false)
    private LocalDateTime updatedOn = LocalDateTime.now();

    // Navigation
    @OneToMany(mappedBy = "reconciliationReport", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReconciliationException> exceptions = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_batch_id", insertable = false, updatable = false)
    private SettlementBatch settlementBatch;

    @PreUpdate
    public void preUpdate() { this.updatedOn = LocalDateTime.now(); }

    public ReconciliationReport() {
    }

    public ReconciliationReport(String id, String reportReference, ReconciliationReportType reportType, ReconciliationReportStatus status, String settlementBatchId, LocalDate reconciliationDate, LocalDateTime periodStart, LocalDateTime periodEnd, String currency, BigDecimal internalTotalCredits, BigDecimal internalTotalDebits, int internalTransactionCount, BigDecimal externalTotalCredits, BigDecimal externalTotalDebits, Integer externalTransactionCount, BigDecimal creditVariance, BigDecimal debitVariance, int matchedCount, int unmatchedCount, int exceptionCount, String externalStatementUrl, String reportFileUrl, String reconciledBy, LocalDateTime reconciledAt, String reviewedBy, LocalDateTime reviewedAt, String reviewNote, String notes, LocalDateTime createdOn, LocalDateTime updatedOn, List<ReconciliationException> exceptions, SettlementBatch settlementBatch) {
        this.id = id;
        this.reportReference = reportReference;
        this.reportType = reportType;
        this.status = status;
        this.settlementBatchId = settlementBatchId;
        this.reconciliationDate = reconciliationDate;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.currency = currency;
        this.internalTotalCredits = internalTotalCredits;
        this.internalTotalDebits = internalTotalDebits;
        this.internalTransactionCount = internalTransactionCount;
        this.externalTotalCredits = externalTotalCredits;
        this.externalTotalDebits = externalTotalDebits;
        this.externalTransactionCount = externalTransactionCount;
        this.creditVariance = creditVariance;
        this.debitVariance = debitVariance;
        this.matchedCount = matchedCount;
        this.unmatchedCount = unmatchedCount;
        this.exceptionCount = exceptionCount;
        this.externalStatementUrl = externalStatementUrl;
        this.reportFileUrl = reportFileUrl;
        this.reconciledBy = reconciledBy;
        this.reconciledAt = reconciledAt;
        this.reviewedBy = reviewedBy;
        this.reviewedAt = reviewedAt;
        this.reviewNote = reviewNote;
        this.notes = notes;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
        this.exceptions = exceptions;
        this.settlementBatch = settlementBatch;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getReportReference() { return reportReference; }
    public void setReportReference(String reportReference) { this.reportReference = reportReference; }

    public ReconciliationReportType getReportType() { return reportType; }
    public void setReportType(ReconciliationReportType reportType) { this.reportType = reportType; }

    public ReconciliationReportStatus getStatus() { return status; }
    public void setStatus(ReconciliationReportStatus status) { this.status = status; }

    public String getSettlementBatchId() { return settlementBatchId; }
    public void setSettlementBatchId(String settlementBatchId) { this.settlementBatchId = settlementBatchId; }

    public LocalDate getReconciliationDate() { return reconciliationDate; }
    public void setReconciliationDate(LocalDate reconciliationDate) { this.reconciliationDate = reconciliationDate; }

    public LocalDateTime getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDateTime periodStart) { this.periodStart = periodStart; }

    public LocalDateTime getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDateTime periodEnd) { this.periodEnd = periodEnd; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getInternalTotalCredits() { return internalTotalCredits; }
    public void setInternalTotalCredits(BigDecimal internalTotalCredits) { this.internalTotalCredits = internalTotalCredits; }

    public BigDecimal getInternalTotalDebits() { return internalTotalDebits; }
    public void setInternalTotalDebits(BigDecimal internalTotalDebits) { this.internalTotalDebits = internalTotalDebits; }

    public int getInternalTransactionCount() { return internalTransactionCount; }
    public void setInternalTransactionCount(int internalTransactionCount) { this.internalTransactionCount = internalTransactionCount; }

    public BigDecimal getExternalTotalCredits() { return externalTotalCredits; }
    public void setExternalTotalCredits(BigDecimal externalTotalCredits) { this.externalTotalCredits = externalTotalCredits; }

    public BigDecimal getExternalTotalDebits() { return externalTotalDebits; }
    public void setExternalTotalDebits(BigDecimal externalTotalDebits) { this.externalTotalDebits = externalTotalDebits; }

    public Integer getExternalTransactionCount() { return externalTransactionCount; }
    public void setExternalTransactionCount(Integer externalTransactionCount) { this.externalTransactionCount = externalTransactionCount; }

    public BigDecimal getCreditVariance() { return creditVariance; }
    public void setCreditVariance(BigDecimal creditVariance) { this.creditVariance = creditVariance; }

    public BigDecimal getDebitVariance() { return debitVariance; }
    public void setDebitVariance(BigDecimal debitVariance) { this.debitVariance = debitVariance; }

    public int getMatchedCount() { return matchedCount; }
    public void setMatchedCount(int matchedCount) { this.matchedCount = matchedCount; }

    public int getUnmatchedCount() { return unmatchedCount; }
    public void setUnmatchedCount(int unmatchedCount) { this.unmatchedCount = unmatchedCount; }

    public int getExceptionCount() { return exceptionCount; }
    public void setExceptionCount(int exceptionCount) { this.exceptionCount = exceptionCount; }

    public String getExternalStatementUrl() { return externalStatementUrl; }
    public void setExternalStatementUrl(String externalStatementUrl) { this.externalStatementUrl = externalStatementUrl; }

    public String getReportFileUrl() { return reportFileUrl; }
    public void setReportFileUrl(String reportFileUrl) { this.reportFileUrl = reportFileUrl; }

    public String getReconciledBy() { return reconciledBy; }
    public void setReconciledBy(String reconciledBy) { this.reconciledBy = reconciledBy; }

    public LocalDateTime getReconciledAt() { return reconciledAt; }
    public void setReconciledAt(LocalDateTime reconciledAt) { this.reconciledAt = reconciledAt; }

    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }

    public String getReviewNote() { return reviewNote; }
    public void setReviewNote(String reviewNote) { this.reviewNote = reviewNote; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }

    public LocalDateTime getUpdatedOn() { return updatedOn; }
    public void setUpdatedOn(LocalDateTime updatedOn) { this.updatedOn = updatedOn; }

    public List<ReconciliationException> getExceptions() { return exceptions; }
    public void setExceptions(List<ReconciliationException> exceptions) { this.exceptions = exceptions; }

    public SettlementBatch getSettlementBatch() { return settlementBatch; }
    public void setSettlementBatch(SettlementBatch settlementBatch) { this.settlementBatch = settlementBatch; }
}
