package com.example.admin_api_service.models.settlementsAndReconciliation;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.example.admin_api_service.enums.SettlementBatchStatus;
import com.example.admin_api_service.enums.SettlementBatchType;

@Entity
@Table(name = "settlement_batches")
public class SettlementBatch {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id = UUID.randomUUID().toString();

    // Human-readable reference e.g. STL-20240315-001
    @Column(name = "batch_reference", length = 50, nullable = false, unique = true)
    private String batchReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "batch_type", length = 30, nullable = false)
    private SettlementBatchType batchType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private SettlementBatchStatus status = SettlementBatchStatus.PENDING;

    // Settlement period
    @Column(name = "settlement_date", nullable = false)
    private LocalDate settlementDate;

    @Column(name = "period_start", nullable = false)
    private LocalDateTime periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDateTime periodEnd;

    @Column(name = "currency", length = 10, nullable = false)
    private String currency;

    // Counterparty bank or payment processor being settled with
    @Column(name = "counterparty_name", length = 200)
    private String counterpartyName;

    @Column(name = "counterparty_bank_code", length = 20)
    private String counterpartyBankCode;

    @Column(name = "counterparty_account_number", length = 30)
    private String counterpartyAccountNumber;

    // Financial totals
    @Column(name = "total_credit_amount", precision = 18, scale = 4, nullable = false)
    private BigDecimal totalCreditAmount = BigDecimal.ZERO;

    @Column(name = "total_debit_amount", precision = 18, scale = 4, nullable = false)
    private BigDecimal totalDebitAmount = BigDecimal.ZERO;

    @Column(name = "total_fee_amount", precision = 18, scale = 4, nullable = false)
    private BigDecimal totalFeeAmount = BigDecimal.ZERO;

    @Column(name = "net_settlement_amount", precision = 18, scale = 4, nullable = false)
    private BigDecimal netSettlementAmount = BigDecimal.ZERO;

    @Column(name = "total_records", nullable = false)
    private int totalRecords = 0;

    @Column(name = "successful_records", nullable = false)
    private int successfulRecords = 0;

    @Column(name = "failed_records", nullable = false)
    private int failedRecords = 0;

    // Reference from the counterparty or payment processor confirming settlement
    @Column(name = "external_settlement_reference", length = 100)
    private String externalSettlementReference;

    // Linked system wallet debited/credited for net settlement
    @Column(name = "system_wallet_id")
    private Long systemWalletId;

    @Column(name = "initiated_by", length = 36)
    private String initiatedBy;

    @Column(name = "approved_by", length = 36)
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn = LocalDateTime.now();

    @Column(name = "updated_on", nullable = false)
    private LocalDateTime updatedOn = LocalDateTime.now();

    // Navigation
    @OneToMany(mappedBy = "settlementBatch", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SettlementRecord> records = new ArrayList<>();

    @PreUpdate
    public void preUpdate() { this.updatedOn = LocalDateTime.now(); }

    public SettlementBatch() {
    }

    public SettlementBatch(String id, String batchReference, SettlementBatchType batchType, SettlementBatchStatus status, LocalDate settlementDate, LocalDateTime periodStart, LocalDateTime periodEnd, String currency, String counterpartyName, String counterpartyBankCode, String counterpartyAccountNumber, BigDecimal totalCreditAmount, BigDecimal totalDebitAmount, BigDecimal totalFeeAmount, BigDecimal netSettlementAmount, int totalRecords, int successfulRecords, int failedRecords, String externalSettlementReference, Long systemWalletId, String initiatedBy, String approvedBy, LocalDateTime approvedAt, LocalDateTime processedAt, LocalDateTime completedAt, String failureReason, String notes, LocalDateTime createdOn, LocalDateTime updatedOn, List<SettlementRecord> records) {
        this.id = id;
        this.batchReference = batchReference;
        this.batchType = batchType;
        this.status = status;
        this.settlementDate = settlementDate;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.currency = currency;
        this.counterpartyName = counterpartyName;
        this.counterpartyBankCode = counterpartyBankCode;
        this.counterpartyAccountNumber = counterpartyAccountNumber;
        this.totalCreditAmount = totalCreditAmount;
        this.totalDebitAmount = totalDebitAmount;
        this.totalFeeAmount = totalFeeAmount;
        this.netSettlementAmount = netSettlementAmount;
        this.totalRecords = totalRecords;
        this.successfulRecords = successfulRecords;
        this.failedRecords = failedRecords;
        this.externalSettlementReference = externalSettlementReference;
        this.systemWalletId = systemWalletId;
        this.initiatedBy = initiatedBy;
        this.approvedBy = approvedBy;
        this.approvedAt = approvedAt;
        this.processedAt = processedAt;
        this.completedAt = completedAt;
        this.failureReason = failureReason;
        this.notes = notes;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
        this.records = records;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBatchReference() { return batchReference; }
    public void setBatchReference(String batchReference) { this.batchReference = batchReference; }

    public SettlementBatchType getBatchType() { return batchType; }
    public void setBatchType(SettlementBatchType batchType) { this.batchType = batchType; }

    public SettlementBatchStatus getStatus() { return status; }
    public void setStatus(SettlementBatchStatus status) { this.status = status; }

    public LocalDate getSettlementDate() { return settlementDate; }
    public void setSettlementDate(LocalDate settlementDate) { this.settlementDate = settlementDate; }

    public LocalDateTime getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDateTime periodStart) { this.periodStart = periodStart; }

    public LocalDateTime getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDateTime periodEnd) { this.periodEnd = periodEnd; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getCounterpartyName() { return counterpartyName; }
    public void setCounterpartyName(String counterpartyName) { this.counterpartyName = counterpartyName; }

    public String getCounterpartyBankCode() { return counterpartyBankCode; }
    public void setCounterpartyBankCode(String counterpartyBankCode) { this.counterpartyBankCode = counterpartyBankCode; }

    public String getCounterpartyAccountNumber() { return counterpartyAccountNumber; }
    public void setCounterpartyAccountNumber(String counterpartyAccountNumber) { this.counterpartyAccountNumber = counterpartyAccountNumber; }

    public BigDecimal getTotalCreditAmount() { return totalCreditAmount; }
    public void setTotalCreditAmount(BigDecimal totalCreditAmount) { this.totalCreditAmount = totalCreditAmount; }

    public BigDecimal getTotalDebitAmount() { return totalDebitAmount; }
    public void setTotalDebitAmount(BigDecimal totalDebitAmount) { this.totalDebitAmount = totalDebitAmount; }

    public BigDecimal getTotalFeeAmount() { return totalFeeAmount; }
    public void setTotalFeeAmount(BigDecimal totalFeeAmount) { this.totalFeeAmount = totalFeeAmount; }

    public BigDecimal getNetSettlementAmount() { return netSettlementAmount; }
    public void setNetSettlementAmount(BigDecimal netSettlementAmount) { this.netSettlementAmount = netSettlementAmount; }

    public int getTotalRecords() { return totalRecords; }
    public void setTotalRecords(int totalRecords) { this.totalRecords = totalRecords; }

    public int getSuccessfulRecords() { return successfulRecords; }
    public void setSuccessfulRecords(int successfulRecords) { this.successfulRecords = successfulRecords; }

    public int getFailedRecords() { return failedRecords; }
    public void setFailedRecords(int failedRecords) { this.failedRecords = failedRecords; }

    public String getExternalSettlementReference() { return externalSettlementReference; }
    public void setExternalSettlementReference(String externalSettlementReference) { this.externalSettlementReference = externalSettlementReference; }

    public Long getSystemWalletId() { return systemWalletId; }
    public void setSystemWalletId(Long systemWalletId) { this.systemWalletId = systemWalletId; }

    public String getInitiatedBy() { return initiatedBy; }
    public void setInitiatedBy(String initiatedBy) { this.initiatedBy = initiatedBy; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }

    public LocalDateTime getUpdatedOn() { return updatedOn; }
    public void setUpdatedOn(LocalDateTime updatedOn) { this.updatedOn = updatedOn; }

    public List<SettlementRecord> getRecords() { return records; }
    public void setRecords(List<SettlementRecord> records) { this.records = records; }
}
