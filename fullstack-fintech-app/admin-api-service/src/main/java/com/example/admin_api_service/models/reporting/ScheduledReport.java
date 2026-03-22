package com.example.admin_api_service.models.reporting;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import com.example.admin_api_service.enums.ReportDeliveryChannel;
import com.example.admin_api_service.enums.ReportFormat;
import com.example.admin_api_service.enums.ReportFrequency;
import com.example.admin_api_service.enums.ScheduledReportStatus;
import com.example.admin_api_service.enums.ScheduledReportType;

@Entity
@Table(name = "scheduled_reports")
public class ScheduledReport {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id = UUID.randomUUID().toString();

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", length = 50, nullable = false)
    private ScheduledReportType reportType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private ScheduledReportStatus status = ScheduledReportStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", length = 20, nullable = false)
    private ReportFrequency frequency;

    @Enumerated(EnumType.STRING)
    @Column(name = "format", length = 20, nullable = false)
    private ReportFormat format = ReportFormat.PDF;

    // Cron expression for custom schedules e.g. "0 8 * * MON"
    @Column(name = "cron_expression", length = 100)
    private String cronExpression;

    // Time of day to run the report (UTC)
    @Column(name = "run_at_time")
    private LocalTime runAtTime;

    // Day of week for WEEKLY reports (1=Monday ... 7=Sunday)
    @Column(name = "run_on_day_of_week")
    private Integer runOnDayOfWeek;

    // Day of month for MONTHLY reports (1–28)
    @Column(name = "run_on_day_of_month")
    private Integer runOnDayOfMonth;

    // Report-specific filter parameters stored as JSON
    // e.g. {"currency": "NGN", "transactionTypes": ["TRANSFER", "WITHDRAWAL"], "kycTier": "TIER_2"}
    @Column(name = "filter_parameters", columnDefinition = "json")
    private String filterParameters;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_channel", length = 20, nullable = false)
    private ReportDeliveryChannel deliveryChannel;

    // Email addresses to deliver the report to stored as JSON array
    @Column(name = "delivery_recipients", columnDefinition = "json")
    private String deliveryRecipients;

    // Admin user IDs who should receive the report stored as JSON array
    @Column(name = "recipient_admin_ids", columnDefinition = "json")
    private String recipientAdminIds;

    // Storage path/bucket where exported files are saved
    @Column(name = "storage_path", length = 500)
    private String storagePath;

    // Retention period in days before old exports are deleted — null means keep forever
    @Column(name = "retention_days")
    private Integer retentionDays;

    @Column(name = "last_run_at")
    private LocalDateTime lastRunAt;

    @Column(name = "last_run_status", length = 20)
    private String lastRunStatus;

    @Column(name = "next_run_at")
    private LocalDateTime nextRunAt;

    @Column(name = "total_runs", nullable = false)
    private int totalRuns = 0;

    @Column(name = "failed_runs", nullable = false)
    private int failedRuns = 0;

    @Column(name = "created_by", length = 36, nullable = false)
    private String createdBy;

    @Column(name = "updated_by", length = 36)
    private String updatedBy;

    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn = LocalDateTime.now();

    @Column(name = "updated_on", nullable = false)
    private LocalDateTime updatedOn = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() { this.updatedOn = LocalDateTime.now(); }

    public ScheduledReport() {
    }

    public ScheduledReport(String id, String name, String description, ScheduledReportType reportType, ScheduledReportStatus status, ReportFrequency frequency, ReportFormat format, String cronExpression, LocalTime runAtTime, Integer runOnDayOfWeek, Integer runOnDayOfMonth, String filterParameters, ReportDeliveryChannel deliveryChannel, String deliveryRecipients, String recipientAdminIds, String storagePath, Integer retentionDays, LocalDateTime lastRunAt, String lastRunStatus, LocalDateTime nextRunAt, int totalRuns, int failedRuns, String createdBy, String updatedBy, LocalDateTime createdOn, LocalDateTime updatedOn) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.reportType = reportType;
        this.status = status;
        this.frequency = frequency;
        this.format = format;
        this.cronExpression = cronExpression;
        this.runAtTime = runAtTime;
        this.runOnDayOfWeek = runOnDayOfWeek;
        this.runOnDayOfMonth = runOnDayOfMonth;
        this.filterParameters = filterParameters;
        this.deliveryChannel = deliveryChannel;
        this.deliveryRecipients = deliveryRecipients;
        this.recipientAdminIds = recipientAdminIds;
        this.storagePath = storagePath;
        this.retentionDays = retentionDays;
        this.lastRunAt = lastRunAt;
        this.lastRunStatus = lastRunStatus;
        this.nextRunAt = nextRunAt;
        this.totalRuns = totalRuns;
        this.failedRuns = failedRuns;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ScheduledReportType getReportType() { return reportType; }
    public void setReportType(ScheduledReportType reportType) { this.reportType = reportType; }

    public ScheduledReportStatus getStatus() { return status; }
    public void setStatus(ScheduledReportStatus status) { this.status = status; }

    public ReportFrequency getFrequency() { return frequency; }
    public void setFrequency(ReportFrequency frequency) { this.frequency = frequency; }

    public ReportFormat getFormat() { return format; }
    public void setFormat(ReportFormat format) { this.format = format; }

    public String getCronExpression() { return cronExpression; }
    public void setCronExpression(String cronExpression) { this.cronExpression = cronExpression; }

    public LocalTime getRunAtTime() { return runAtTime; }
    public void setRunAtTime(LocalTime runAtTime) { this.runAtTime = runAtTime; }

    public Integer getRunOnDayOfWeek() { return runOnDayOfWeek; }
    public void setRunOnDayOfWeek(Integer runOnDayOfWeek) { this.runOnDayOfWeek = runOnDayOfWeek; }

    public Integer getRunOnDayOfMonth() { return runOnDayOfMonth; }
    public void setRunOnDayOfMonth(Integer runOnDayOfMonth) { this.runOnDayOfMonth = runOnDayOfMonth; }

    public String getFilterParameters() { return filterParameters; }
    public void setFilterParameters(String filterParameters) { this.filterParameters = filterParameters; }

    public ReportDeliveryChannel getDeliveryChannel() { return deliveryChannel; }
    public void setDeliveryChannel(ReportDeliveryChannel deliveryChannel) { this.deliveryChannel = deliveryChannel; }

    public String getDeliveryRecipients() { return deliveryRecipients; }
    public void setDeliveryRecipients(String deliveryRecipients) { this.deliveryRecipients = deliveryRecipients; }

    public String getRecipientAdminIds() { return recipientAdminIds; }
    public void setRecipientAdminIds(String recipientAdminIds) { this.recipientAdminIds = recipientAdminIds; }

    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }

    public Integer getRetentionDays() { return retentionDays; }
    public void setRetentionDays(Integer retentionDays) { this.retentionDays = retentionDays; }

    public LocalDateTime getLastRunAt() { return lastRunAt; }
    public void setLastRunAt(LocalDateTime lastRunAt) { this.lastRunAt = lastRunAt; }

    public String getLastRunStatus() { return lastRunStatus; }
    public void setLastRunStatus(String lastRunStatus) { this.lastRunStatus = lastRunStatus; }

    public LocalDateTime getNextRunAt() { return nextRunAt; }
    public void setNextRunAt(LocalDateTime nextRunAt) { this.nextRunAt = nextRunAt; }

    public int getTotalRuns() { return totalRuns; }
    public void setTotalRuns(int totalRuns) { this.totalRuns = totalRuns; }

    public int getFailedRuns() { return failedRuns; }
    public void setFailedRuns(int failedRuns) { this.failedRuns = failedRuns; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }

    public LocalDateTime getUpdatedOn() { return updatedOn; }
    public void setUpdatedOn(LocalDateTime updatedOn) { this.updatedOn = updatedOn; }
}