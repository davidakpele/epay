package com.example.admin_api_service.models.complianceAndRisk;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.admin_api_service.enums.ComplianceReportFormat;
import com.example.admin_api_service.enums.ComplianceReportStatus;
import com.example.admin_api_service.enums.ComplianceReportType;
import jakarta.persistence.*;

@Entity
@Table(name = "compliance_reports")
public class ComplianceReport {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id = UUID.randomUUID().toString();

    // Human-readable reference e.g. CR-2024-00045
    @Column(name = "report_reference", length = 50, nullable = false, unique = true)
    private String reportReference;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", length = 50, nullable = false)
    private ComplianceReportType reportType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private ComplianceReportStatus status = ComplianceReportStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "format", length = 20, nullable = false)
    private ComplianceReportFormat format = ComplianceReportFormat.PDF;

    // Reporting period
    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    // Regulatory body this report is submitted to e.g. CBN, NFIU, EFCC
    @Column(name = "regulatory_body", length = 100)
    private String regulatoryBody;

    // Submission reference number from the regulatory body
    @Column(name = "submission_reference", length = 100)
    private String submissionReference;

    @Column(name = "summary", length = 2000)
    private String summary;

    // Report data snapshot stored as JSON
    @Column(name = "report_data", columnDefinition = "json")
    private String reportData;

    // Storage path / URL of the generated report file
    @Column(name = "file_url", length = 500)
    private String fileUrl;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    // Linked AML cases included in this report stored as JSON array of IDs
    @Column(name = "aml_case_ids", columnDefinition = "json")
    private String amlCaseIds;

    // Total SAR count included
    @Column(name = "sar_count", nullable = false)
    private int sarCount = 0;

    // Total flagged transactions included
    @Column(name = "flagged_transaction_count", nullable = false)
    private int flaggedTransactionCount = 0;

    @Column(name = "generated_by", length = 36)
    private String generatedBy;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @Column(name = "reviewed_by", length = 36)
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "review_note", length = 500)
    private String reviewNote;

    @Column(name = "approved_by", length = 36)
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "submitted_by", length = 36)
    private String submittedBy;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn = LocalDateTime.now();

    @Column(name = "updated_on", nullable = false)
    private LocalDateTime updatedOn = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedOn = LocalDateTime.now();
    }

    public ComplianceReport() {
    }

    public ComplianceReport(String id, String reportReference, String title, ComplianceReportType reportType, ComplianceReportStatus status, ComplianceReportFormat format, LocalDate periodStart, LocalDate periodEnd, String regulatoryBody, String submissionReference, String summary, String reportData, String fileUrl, Long fileSizeBytes, String amlCaseIds, int sarCount, int flaggedTransactionCount, String generatedBy, LocalDateTime generatedAt, String reviewedBy, LocalDateTime reviewedAt, String reviewNote, String approvedBy, LocalDateTime approvedAt, String submittedBy, LocalDateTime submittedAt, LocalDate dueDate, String rejectionReason, LocalDateTime createdOn, LocalDateTime updatedOn) {
        this.id = id;
        this.reportReference = reportReference;
        this.title = title;
        this.reportType = reportType;
        this.status = status;
        this.format = format;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.regulatoryBody = regulatoryBody;
        this.submissionReference = submissionReference;
        this.summary = summary;
        this.reportData = reportData;
        this.fileUrl = fileUrl;
        this.fileSizeBytes = fileSizeBytes;
        this.amlCaseIds = amlCaseIds;
        this.sarCount = sarCount;
        this.flaggedTransactionCount = flaggedTransactionCount;
        this.generatedBy = generatedBy;
        this.generatedAt = generatedAt;
        this.reviewedBy = reviewedBy;
        this.reviewedAt = reviewedAt;
        this.reviewNote = reviewNote;
        this.approvedBy = approvedBy;
        this.approvedAt = approvedAt;
        this.submittedBy = submittedBy;
        this.submittedAt = submittedAt;
        this.dueDate = dueDate;
        this.rejectionReason = rejectionReason;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getReportReference() { return reportReference; }
    public void setReportReference(String reportReference) { this.reportReference = reportReference; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public ComplianceReportType getReportType() { return reportType; }
    public void setReportType(ComplianceReportType reportType) { this.reportType = reportType; }

    public ComplianceReportStatus getStatus() { return status; }
    public void setStatus(ComplianceReportStatus status) { this.status = status; }

    public ComplianceReportFormat getFormat() { return format; }
    public void setFormat(ComplianceReportFormat format) { this.format = format; }

    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }

    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }

    public String getRegulatoryBody() { return regulatoryBody; }
    public void setRegulatoryBody(String regulatoryBody) { this.regulatoryBody = regulatoryBody; }

    public String getSubmissionReference() { return submissionReference; }
    public void setSubmissionReference(String submissionReference) { this.submissionReference = submissionReference; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getReportData() { return reportData; }
    public void setReportData(String reportData) { this.reportData = reportData; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public Long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(Long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }

    public String getAmlCaseIds() { return amlCaseIds; }
    public void setAmlCaseIds(String amlCaseIds) { this.amlCaseIds = amlCaseIds; }

    public int getSarCount() { return sarCount; }
    public void setSarCount(int sarCount) { this.sarCount = sarCount; }

    public int getFlaggedTransactionCount() { return flaggedTransactionCount; }
    public void setFlaggedTransactionCount(int flaggedTransactionCount) { this.flaggedTransactionCount = flaggedTransactionCount; }

    public String getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(String generatedBy) { this.generatedBy = generatedBy; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }

    public String getReviewNote() { return reviewNote; }
    public void setReviewNote(String reviewNote) { this.reviewNote = reviewNote; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }

    public String getSubmittedBy() { return submittedBy; }
    public void setSubmittedBy(String submittedBy) { this.submittedBy = submittedBy; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }

    public LocalDateTime getUpdatedOn() { return updatedOn; }
    public void setUpdatedOn(LocalDateTime updatedOn) { this.updatedOn = updatedOn; }
}
