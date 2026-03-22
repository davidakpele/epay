package com.example.admin_api_service.models.accessAndApprovals;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.admin_api_service.enums.BulkOperationStatus;
import com.example.admin_api_service.enums.BulkOperationType;

@Entity
@Table(name = "bulk_operation_jobs")
public class BulkOperationJob {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id = UUID.randomUUID().toString();

    // Linked approval request if this job required sign-off
    @Column(name = "approval_request_id", length = 36)
    private String approvalRequestId;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", length = 50, nullable = false)
    private BulkOperationType operationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private BulkOperationStatus status = BulkOperationStatus.PENDING;

    // Input file path or source reference (e.g. S3 key, GCS path)
    @Column(name = "source_reference", length = 500)
    private String sourceReference;

    // Full job parameters stored as JSON
    @Column(name = "parameters", columnDefinition = "json")
    private String parameters;

    @Column(name = "total_records", nullable = false)
    private int totalRecords = 0;

    @Column(name = "processed_records", nullable = false)
    private int processedRecords = 0;

    @Column(name = "successful_records", nullable = false)
    private int successfulRecords = 0;

    @Column(name = "failed_records", nullable = false)
    private int failedRecords = 0;

    @Column(name = "skipped_records", nullable = false)
    private int skippedRecords = 0;

    // Path/URL to downloadable error report
    @Column(name = "error_report_url", length = 500)
    private String errorReportUrl;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_by", length = 36, nullable = false)
    private String createdBy;

    @Column(name = "approved_by", length = 36)
    private String approvedBy;

    @Column(name = "cancelled_by", length = 36)
    private String cancelledBy;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn = LocalDateTime.now();

    @Column(name = "updated_on", nullable = false)
    private LocalDateTime updatedOn = LocalDateTime.now();

    // Navigation
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BulkOperationRecord> records = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approval_request_id", insertable = false, updatable = false)
    private ApprovalRequest approvalRequest;

    @PreUpdate
    public void preUpdate() {
        this.updatedOn = LocalDateTime.now();
    }

    public BulkOperationJob() {
    }

    public BulkOperationJob(String id, String approvalRequestId, String name, BulkOperationType operationType, BulkOperationStatus status, String sourceReference, String parameters, int totalRecords, int processedRecords, int successfulRecords, int failedRecords, int skippedRecords, String errorReportUrl, String failureReason, LocalDateTime scheduledAt, LocalDateTime startedAt, LocalDateTime completedAt, String createdBy, String approvedBy, String cancelledBy, String cancellationReason, String ipAddress, LocalDateTime createdOn, LocalDateTime updatedOn, List<BulkOperationRecord> records, ApprovalRequest approvalRequest) {
        this.id = id;
        this.approvalRequestId = approvalRequestId;
        this.name = name;
        this.operationType = operationType;
        this.status = status;
        this.sourceReference = sourceReference;
        this.parameters = parameters;
        this.totalRecords = totalRecords;
        this.processedRecords = processedRecords;
        this.successfulRecords = successfulRecords;
        this.failedRecords = failedRecords;
        this.skippedRecords = skippedRecords;
        this.errorReportUrl = errorReportUrl;
        this.failureReason = failureReason;
        this.scheduledAt = scheduledAt;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.createdBy = createdBy;
        this.approvedBy = approvedBy;
        this.cancelledBy = cancelledBy;
        this.cancellationReason = cancellationReason;
        this.ipAddress = ipAddress;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
        this.records = records;
        this.approvalRequest = approvalRequest;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getApprovalRequestId() { return approvalRequestId; }
    public void setApprovalRequestId(String approvalRequestId) { this.approvalRequestId = approvalRequestId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BulkOperationType getOperationType() { return operationType; }
    public void setOperationType(BulkOperationType operationType) { this.operationType = operationType; }

    public BulkOperationStatus getStatus() { return status; }
    public void setStatus(BulkOperationStatus status) { this.status = status; }

    public String getSourceReference() { return sourceReference; }
    public void setSourceReference(String sourceReference) { this.sourceReference = sourceReference; }

    public String getParameters() { return parameters; }
    public void setParameters(String parameters) { this.parameters = parameters; }

    public int getTotalRecords() { return totalRecords; }
    public void setTotalRecords(int totalRecords) { this.totalRecords = totalRecords; }

    public int getProcessedRecords() { return processedRecords; }
    public void setProcessedRecords(int processedRecords) { this.processedRecords = processedRecords; }

    public int getSuccessfulRecords() { return successfulRecords; }
    public void setSuccessfulRecords(int successfulRecords) { this.successfulRecords = successfulRecords; }

    public int getFailedRecords() { return failedRecords; }
    public void setFailedRecords(int failedRecords) { this.failedRecords = failedRecords; }

    public int getSkippedRecords() { return skippedRecords; }
    public void setSkippedRecords(int skippedRecords) { this.skippedRecords = skippedRecords; }

    public String getErrorReportUrl() { return errorReportUrl; }
    public void setErrorReportUrl(String errorReportUrl) { this.errorReportUrl = errorReportUrl; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public String getCancelledBy() { return cancelledBy; }
    public void setCancelledBy(String cancelledBy) { this.cancelledBy = cancelledBy; }

    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }

    public LocalDateTime getUpdatedOn() { return updatedOn; }
    public void setUpdatedOn(LocalDateTime updatedOn) { this.updatedOn = updatedOn; }

    public List<BulkOperationRecord> getRecords() { return records; }
    public void setRecords(List<BulkOperationRecord> records) { this.records = records; }

    public ApprovalRequest getApprovalRequest() { return approvalRequest; }
    public void setApprovalRequest(ApprovalRequest approvalRequest) { this.approvalRequest = approvalRequest; }
}