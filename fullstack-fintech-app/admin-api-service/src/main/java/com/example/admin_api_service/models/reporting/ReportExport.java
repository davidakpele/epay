package com.example.admin_api_service.models.reporting;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.admin_api_service.enums.ReportExportStatus;
import com.example.admin_api_service.enums.ReportFormat;
import com.example.admin_api_service.enums.ScheduledReportType;

@Entity
@Table(name = "report_exports")
public class ReportExport {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id = UUID.randomUUID().toString();

    // Linked scheduled report — null for ad-hoc exports
    @Column(name = "scheduled_report_id", length = 36)
    private String scheduledReportId;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", length = 50, nullable = false)
    private ScheduledReportType reportType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private ReportExportStatus status = ReportExportStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "format", length = 20, nullable = false)
    private ReportFormat format;

    // Period covered by this export
    @Column(name = "period_start")
    private LocalDateTime periodStart;

    @Column(name = "period_end")
    private LocalDateTime periodEnd;

    // Filter parameters applied to generate this export stored as JSON
    @Column(name = "filter_parameters", columnDefinition = "json")
    private String filterParameters;

    // Storage path of the generated file e.g. S3 key or GCS path
    @Column(name = "file_url", length = 500)
    private String fileUrl;

    @Column(name = "file_name", length = 200)
    private String fileName;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    // SHA-256 checksum of the file for integrity verification
    @Column(name = "file_checksum", length = 64)
    private String fileChecksum;

    // Number of data rows in the export
    @Column(name = "row_count")
    private Long rowCount;

    // Signed download URL — regenerated on each access request
    @Column(name = "download_url", length = 500)
    private String downloadUrl;

    // When the signed download URL expires
    @Column(name = "download_url_expires_at")
    private LocalDateTime downloadUrlExpiresAt;

    // When this export file will be permanently deleted
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    // Duration taken to generate the report in milliseconds
    @Column(name = "generation_duration_ms")
    private Long generationDurationMs;

    // Whether this export was triggered by a scheduled run or manually
    @Column(name = "is_manual", nullable = false)
    private boolean isManual = false;

    @Column(name = "requested_by", length = 36)
    private String requestedBy;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn = LocalDateTime.now();

    @Column(name = "updated_on", nullable = false)
    private LocalDateTime updatedOn = LocalDateTime.now();

    // Navigation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scheduled_report_id", insertable = false, updatable = false)
    private ScheduledReport scheduledReport;

    @PreUpdate
    public void preUpdate() { this.updatedOn = LocalDateTime.now(); }

    public ReportExport() {
    }

    public ReportExport(String id, String scheduledReportId, ScheduledReportType reportType, ReportExportStatus status, ReportFormat format, LocalDateTime periodStart, LocalDateTime periodEnd, String filterParameters, String fileUrl, String fileName, Long fileSizeBytes, String fileChecksum, Long rowCount, String downloadUrl, LocalDateTime downloadUrlExpiresAt, LocalDateTime expiresAt, String failureReason, Long generationDurationMs, boolean isManual, String requestedBy, LocalDateTime startedAt, LocalDateTime completedAt, LocalDateTime createdOn, LocalDateTime updatedOn, ScheduledReport scheduledReport) {
        this.id = id;
        this.scheduledReportId = scheduledReportId;
        this.reportType = reportType;
        this.status = status;
        this.format = format;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.filterParameters = filterParameters;
        this.fileUrl = fileUrl;
        this.fileName = fileName;
        this.fileSizeBytes = fileSizeBytes;
        this.fileChecksum = fileChecksum;
        this.rowCount = rowCount;
        this.downloadUrl = downloadUrl;
        this.downloadUrlExpiresAt = downloadUrlExpiresAt;
        this.expiresAt = expiresAt;
        this.failureReason = failureReason;
        this.generationDurationMs = generationDurationMs;
        this.isManual = isManual;
        this.requestedBy = requestedBy;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
        this.scheduledReport = scheduledReport;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getScheduledReportId() { return scheduledReportId; }
    public void setScheduledReportId(String scheduledReportId) { this.scheduledReportId = scheduledReportId; }

    public ScheduledReportType getReportType() { return reportType; }
    public void setReportType(ScheduledReportType reportType) { this.reportType = reportType; }

    public ReportExportStatus getStatus() { return status; }
    public void setStatus(ReportExportStatus status) { this.status = status; }

    public ReportFormat getFormat() { return format; }
    public void setFormat(ReportFormat format) { this.format = format; }

    public LocalDateTime getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDateTime periodStart) { this.periodStart = periodStart; }

    public LocalDateTime getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDateTime periodEnd) { this.periodEnd = periodEnd; }

    public String getFilterParameters() { return filterParameters; }
    public void setFilterParameters(String filterParameters) { this.filterParameters = filterParameters; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public Long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(Long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }

    public String getFileChecksum() { return fileChecksum; }
    public void setFileChecksum(String fileChecksum) { this.fileChecksum = fileChecksum; }

    public Long getRowCount() { return rowCount; }
    public void setRowCount(Long rowCount) { this.rowCount = rowCount; }

    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }

    public LocalDateTime getDownloadUrlExpiresAt() { return downloadUrlExpiresAt; }
    public void setDownloadUrlExpiresAt(LocalDateTime downloadUrlExpiresAt) { this.downloadUrlExpiresAt = downloadUrlExpiresAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public Long getGenerationDurationMs() { return generationDurationMs; }
    public void setGenerationDurationMs(Long generationDurationMs) { this.generationDurationMs = generationDurationMs; }

    public boolean isManual() { return isManual; }
    public void setManual(boolean manual) { isManual = manual; }

    public String getRequestedBy() { return requestedBy; }
    public void setRequestedBy(String requestedBy) { this.requestedBy = requestedBy; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }

    public LocalDateTime getUpdatedOn() { return updatedOn; }
    public void setUpdatedOn(LocalDateTime updatedOn) { this.updatedOn = updatedOn; }

    public ScheduledReport getScheduledReport() { return scheduledReport; }
    public void setScheduledReport(ScheduledReport scheduledReport) { this.scheduledReport = scheduledReport; }
}