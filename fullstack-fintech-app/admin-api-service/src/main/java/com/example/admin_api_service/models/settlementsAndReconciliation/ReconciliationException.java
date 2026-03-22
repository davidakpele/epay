package com.example.admin_api_service.models.settlementsAndReconciliation;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.admin_api_service.enums.ReconciliationExceptionStatus;
import com.example.admin_api_service.enums.ReconciliationExceptionType;

@Entity
@Table(name = "reconciliation_exceptions")
public class ReconciliationException {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id = UUID.randomUUID().toString();

    @Column(name = "reconciliation_report_id", length = 36, nullable = false)
    private String reconciliationReportId;

    @Enumerated(EnumType.STRING)
    @Column(name = "exception_type", length = 40, nullable = false)
    private ReconciliationExceptionType exceptionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private ReconciliationExceptionStatus status = ReconciliationExceptionStatus.OPEN;

    // Internal transaction ID from the ledger (null if missing internally)
    @Column(name = "internal_transaction_id", length = 36)
    private String internalTransactionId;

    // Reference from the external statement (null if missing externally)
    @Column(name = "external_reference", length = 100)
    private String externalReference;

    @Column(name = "currency", length = 10, nullable = false)
    private String currency;

    // Amount recorded internally
    @Column(name = "internal_amount", precision = 18, scale = 4)
    private BigDecimal internalAmount;

    // Amount recorded by the external party
    @Column(name = "external_amount", precision = 18, scale = 4)
    private BigDecimal externalAmount;

    // Difference between internal and external amounts
    @Column(name = "variance_amount", precision = 18, scale = 4)
    private BigDecimal varianceAmount;

    @Column(name = "description", length = 1000, nullable = false)
    private String description;

    @Column(name = "assigned_to", length = 36)
    private String assignedTo;

    @Column(name = "resolved_by", length = 36)
    private String resolvedBy;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolution_note", length = 500)
    private String resolutionNote;

    // Linked manual adjustment if exception was resolved via one
    @Column(name = "manual_adjustment_id", length = 36)
    private String manualAdjustmentId;

    // Linked chargeback case if exception escalated to one
    @Column(name = "chargeback_case_id", length = 36)
    private String chargebackCaseId;

    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn = LocalDateTime.now();

    @Column(name = "updated_on", nullable = false)
    private LocalDateTime updatedOn = LocalDateTime.now();

    // Navigation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reconciliation_report_id", insertable = false, updatable = false)
    private ReconciliationReport reconciliationReport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chargeback_case_id", insertable = false, updatable = false)
    private ChargebackCase chargebackCase;

    @PreUpdate
    public void preUpdate() { this.updatedOn = LocalDateTime.now(); }

    public ReconciliationException() {
    }

    public ReconciliationException(String id, String reconciliationReportId, ReconciliationExceptionType exceptionType, ReconciliationExceptionStatus status, String internalTransactionId, String externalReference, String currency, BigDecimal internalAmount, BigDecimal externalAmount, BigDecimal varianceAmount, String description, String assignedTo, String resolvedBy, LocalDateTime resolvedAt, String resolutionNote, String manualAdjustmentId, String chargebackCaseId, LocalDateTime createdOn, LocalDateTime updatedOn, ReconciliationReport reconciliationReport, ChargebackCase chargebackCase) {
        this.id = id;
        this.reconciliationReportId = reconciliationReportId;
        this.exceptionType = exceptionType;
        this.status = status;
        this.internalTransactionId = internalTransactionId;
        this.externalReference = externalReference;
        this.currency = currency;
        this.internalAmount = internalAmount;
        this.externalAmount = externalAmount;
        this.varianceAmount = varianceAmount;
        this.description = description;
        this.assignedTo = assignedTo;
        this.resolvedBy = resolvedBy;
        this.resolvedAt = resolvedAt;
        this.resolutionNote = resolutionNote;
        this.manualAdjustmentId = manualAdjustmentId;
        this.chargebackCaseId = chargebackCaseId;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
        this.reconciliationReport = reconciliationReport;
        this.chargebackCase = chargebackCase;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getReconciliationReportId() { return reconciliationReportId; }
    public void setReconciliationReportId(String reconciliationReportId) { this.reconciliationReportId = reconciliationReportId; }

    public ReconciliationExceptionType getExceptionType() { return exceptionType; }
    public void setExceptionType(ReconciliationExceptionType exceptionType) { this.exceptionType = exceptionType; }

    public ReconciliationExceptionStatus getStatus() { return status; }
    public void setStatus(ReconciliationExceptionStatus status) { this.status = status; }

    public String getInternalTransactionId() { return internalTransactionId; }
    public void setInternalTransactionId(String internalTransactionId) { this.internalTransactionId = internalTransactionId; }

    public String getExternalReference() { return externalReference; }
    public void setExternalReference(String externalReference) { this.externalReference = externalReference; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getInternalAmount() { return internalAmount; }
    public void setInternalAmount(BigDecimal internalAmount) { this.internalAmount = internalAmount; }

    public BigDecimal getExternalAmount() { return externalAmount; }
    public void setExternalAmount(BigDecimal externalAmount) { this.externalAmount = externalAmount; }

    public BigDecimal getVarianceAmount() { return varianceAmount; }
    public void setVarianceAmount(BigDecimal varianceAmount) { this.varianceAmount = varianceAmount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

    public String getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public String getResolutionNote() { return resolutionNote; }
    public void setResolutionNote(String resolutionNote) { this.resolutionNote = resolutionNote; }

    public String getManualAdjustmentId() { return manualAdjustmentId; }
    public void setManualAdjustmentId(String manualAdjustmentId) { this.manualAdjustmentId = manualAdjustmentId; }

    public String getChargebackCaseId() { return chargebackCaseId; }
    public void setChargebackCaseId(String chargebackCaseId) { this.chargebackCaseId = chargebackCaseId; }

    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }

    public LocalDateTime getUpdatedOn() { return updatedOn; }
    public void setUpdatedOn(LocalDateTime updatedOn) { this.updatedOn = updatedOn; }

    public ReconciliationReport getReconciliationReport() { return reconciliationReport; }
    public void setReconciliationReport(ReconciliationReport reconciliationReport) { this.reconciliationReport = reconciliationReport; }

    public ChargebackCase getChargebackCase() { return chargebackCase; }
    public void setChargebackCase(ChargebackCase chargebackCase) { this.chargebackCase = chargebackCase; }
}
