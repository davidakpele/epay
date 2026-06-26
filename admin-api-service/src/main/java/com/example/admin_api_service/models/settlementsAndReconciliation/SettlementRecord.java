package com.example.admin_api_service.models.settlementsAndReconciliation;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import com.example.admin_api_service.enums.SettlementRecordStatus;
import com.example.admin_api_service.enums.SettlementRecordType;

@Entity
@Table(name = "settlement_records")
public class SettlementRecord {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id = UUID.randomUUID().toString();

    @Column(name = "settlement_batch_id", length = 36, nullable = false)
    private String settlementBatchId;

    // Source transaction from the ledger
    @Column(name = "transaction_id", length = 36, nullable = false)
    private String transactionId;

    @Column(name = "history_reference_id", length = 36)
    private String historyReferenceId;

    @Column(name = "wallet_id", nullable = false)
    private Long walletId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "record_type", length = 20, nullable = false)
    private SettlementRecordType recordType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private SettlementRecordStatus status = SettlementRecordStatus.PENDING;

    @Column(name = "currency", length = 10, nullable = false)
    private String currency;

    @Column(name = "gross_amount", precision = 18, scale = 4, nullable = false)
    private BigDecimal grossAmount;

    @Column(name = "fee_amount", precision = 18, scale = 4, nullable = false)
    private BigDecimal feeAmount = BigDecimal.ZERO;

    @Column(name = "net_amount", precision = 18, scale = 4, nullable = false)
    private BigDecimal netAmount;

    // Counterparty details for this specific record
    @Column(name = "counterparty_bank_code", length = 20)
    private String counterpartyBankCode;

    @Column(name = "counterparty_account_number", length = 30)
    private String counterpartyAccountNumber;

    @Column(name = "counterparty_account_name", length = 100)
    private String counterpartyAccountName;

    // Reference returned by the external settlement processor for this record
    @Column(name = "external_reference", length = 100)
    private String externalReference;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;

    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn = LocalDateTime.now();

    @Column(name = "updated_on", nullable = false)
    private LocalDateTime updatedOn = LocalDateTime.now();

    // Navigation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_batch_id", insertable = false, updatable = false)
    private SettlementBatch settlementBatch;

    @PreUpdate
    public void preUpdate() { this.updatedOn = LocalDateTime.now(); }

    public SettlementRecord() {
    }

    public SettlementRecord(String id, String settlementBatchId, String transactionId, String historyReferenceId, Long walletId, Long userId, SettlementRecordType recordType, SettlementRecordStatus status, String currency, BigDecimal grossAmount, BigDecimal feeAmount, BigDecimal netAmount, String counterpartyBankCode, String counterpartyAccountNumber, String counterpartyAccountName, String externalReference, String failureReason, int retryCount, LocalDateTime settledAt, LocalDateTime createdOn, LocalDateTime updatedOn, SettlementBatch settlementBatch) {
        this.id = id;
        this.settlementBatchId = settlementBatchId;
        this.transactionId = transactionId;
        this.historyReferenceId = historyReferenceId;
        this.walletId = walletId;
        this.userId = userId;
        this.recordType = recordType;
        this.status = status;
        this.currency = currency;
        this.grossAmount = grossAmount;
        this.feeAmount = feeAmount;
        this.netAmount = netAmount;
        this.counterpartyBankCode = counterpartyBankCode;
        this.counterpartyAccountNumber = counterpartyAccountNumber;
        this.counterpartyAccountName = counterpartyAccountName;
        this.externalReference = externalReference;
        this.failureReason = failureReason;
        this.retryCount = retryCount;
        this.settledAt = settledAt;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
        this.settlementBatch = settlementBatch;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSettlementBatchId() { return settlementBatchId; }
    public void setSettlementBatchId(String settlementBatchId) { this.settlementBatchId = settlementBatchId; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getHistoryReferenceId() { return historyReferenceId; }
    public void setHistoryReferenceId(String historyReferenceId) { this.historyReferenceId = historyReferenceId; }

    public Long getWalletId() { return walletId; }
    public void setWalletId(Long walletId) { this.walletId = walletId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public SettlementRecordType getRecordType() { return recordType; }
    public void setRecordType(SettlementRecordType recordType) { this.recordType = recordType; }

    public SettlementRecordStatus getStatus() { return status; }
    public void setStatus(SettlementRecordStatus status) { this.status = status; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getGrossAmount() { return grossAmount; }
    public void setGrossAmount(BigDecimal grossAmount) { this.grossAmount = grossAmount; }

    public BigDecimal getFeeAmount() { return feeAmount; }
    public void setFeeAmount(BigDecimal feeAmount) { this.feeAmount = feeAmount; }

    public BigDecimal getNetAmount() { return netAmount; }
    public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }

    public String getCounterpartyBankCode() { return counterpartyBankCode; }
    public void setCounterpartyBankCode(String counterpartyBankCode) { this.counterpartyBankCode = counterpartyBankCode; }

    public String getCounterpartyAccountNumber() { return counterpartyAccountNumber; }
    public void setCounterpartyAccountNumber(String counterpartyAccountNumber) { this.counterpartyAccountNumber = counterpartyAccountNumber; }

    public String getCounterpartyAccountName() { return counterpartyAccountName; }
    public void setCounterpartyAccountName(String counterpartyAccountName) { this.counterpartyAccountName = counterpartyAccountName; }

    public String getExternalReference() { return externalReference; }
    public void setExternalReference(String externalReference) { this.externalReference = externalReference; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }

    public LocalDateTime getSettledAt() { return settledAt; }
    public void setSettledAt(LocalDateTime settledAt) { this.settledAt = settledAt; }

    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }

    public LocalDateTime getUpdatedOn() { return updatedOn; }
    public void setUpdatedOn(LocalDateTime updatedOn) { this.updatedOn = updatedOn; }

    public SettlementBatch getSettlementBatch() { return settlementBatch; }
    public void setSettlementBatch(SettlementBatch settlementBatch) { this.settlementBatch = settlementBatch; }
}