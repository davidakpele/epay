package com.example.admin_api_service.models.accessAndApprovals;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.admin_api_service.enums.BulkRecordStatus;

@Entity
@Table(name = "bulk_operation_records")
public class BulkOperationRecord {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id = UUID.randomUUID().toString();

    @Column(name = "job_id", length = 36, nullable = false)
    private String jobId;

    // Row number in the source file / batch
    @Column(name = "row_index", nullable = false)
    private int rowIndex;

    // The entity being operated on (walletId, userId, etc.)
    @Column(name = "target_id", length = 36, nullable = false)
    private String targetId;

    // Raw input data for this record stored as JSON
    @Column(name = "input_payload", columnDefinition = "json")
    private String inputPayload;

    // Result data returned after processing stored as JSON
    @Column(name = "result_payload", columnDefinition = "json")
    private String resultPayload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private BulkRecordStatus status = BulkRecordStatus.PENDING;

    @Column(name = "error_code", length = 50)
    private String errorCode;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    // Number of retry attempts for this specific record
    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;

    // Reference to the ledger/history entry created by this record
    @Column(name = "history_reference_id", length = 36)
    private String historyReferenceId;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn = LocalDateTime.now();

    @Column(name = "updated_on", nullable = false)
    private LocalDateTime updatedOn = LocalDateTime.now();

    // Navigation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", insertable = false, updatable = false)
    private BulkOperationJob job;

    @PreUpdate
    public void preUpdate() {
        this.updatedOn = LocalDateTime.now();
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public int getRowIndex() { return rowIndex; }
    public void setRowIndex(int rowIndex) { this.rowIndex = rowIndex; }

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }

    public String getInputPayload() { return inputPayload; }
    public void setInputPayload(String inputPayload) { this.inputPayload = inputPayload; }

    public String getResultPayload() { return resultPayload; }
    public void setResultPayload(String resultPayload) { this.resultPayload = resultPayload; }

    public BulkRecordStatus getStatus() { return status; }
    public void setStatus(BulkRecordStatus status) { this.status = status; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }

    public String getHistoryReferenceId() { return historyReferenceId; }
    public void setHistoryReferenceId(String historyReferenceId) { this.historyReferenceId = historyReferenceId; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }

    public LocalDateTime getUpdatedOn() { return updatedOn; }
    public void setUpdatedOn(LocalDateTime updatedOn) { this.updatedOn = updatedOn; }

    public BulkOperationJob getJob() { return job; }
    public void setJob(BulkOperationJob job) { this.job = job; }
}
