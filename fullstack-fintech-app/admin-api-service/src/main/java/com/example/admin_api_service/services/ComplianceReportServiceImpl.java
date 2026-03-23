package com.example.admin_api_service.services;

import com.example.admin_api_service.Interfaces.IComplianceReportService;
import com.example.admin_api_service.enums.ComplianceReportFormat;
import com.example.admin_api_service.enums.ComplianceReportStatus;
import com.example.admin_api_service.enums.ComplianceReportType;
import com.example.admin_api_service.exceptions.ConflictException;
import com.example.admin_api_service.exceptions.ResourceNotFoundException;
import com.example.admin_api_service.models.complianceAndRisk.ComplianceReport;
import com.example.admin_api_service.repository.ComplianceReportRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Transactional
public class ComplianceReportServiceImpl implements IComplianceReportService {

    private final ComplianceReportRepository complianceReportRepository;
    private static final AtomicLong reportCounter = new AtomicLong();

    public ComplianceReportServiceImpl(ComplianceReportRepository complianceReportRepository) {
        this.complianceReportRepository = complianceReportRepository;
    }

    @Override
    public ComplianceReport createReport(ComplianceReportType reportType, String title,
                                         LocalDate periodStart, LocalDate periodEnd,
                                         String regulatoryBody, ComplianceReportFormat format,
                                         LocalDate dueDate, String createdBy) {
        ComplianceReport report = new ComplianceReport();
        report.setReportReference(generateReportReference());
        report.setReportType(reportType);
        report.setTitle(title);
        report.setPeriodStart(periodStart);
        report.setPeriodEnd(periodEnd);
        report.setRegulatoryBody(regulatoryBody);
        report.setFormat(format);
        report.setDueDate(dueDate);
        report.setStatus(ComplianceReportStatus.DRAFT);
        return complianceReportRepository.save(report);
    }

    @Override
    @Transactional(readOnly = true)
    public ComplianceReport getReportById(String reportId) {
        return complianceReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("ComplianceReport", "id", reportId));
    }

    @Override
    @Transactional(readOnly = true)
    public ComplianceReport getReportByReference(String reportReference) {
        return complianceReportRepository.findByReportReference(reportReference)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ComplianceReport", "reportReference", reportReference));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ComplianceReport> getAllReports(Pageable pageable) {
        return complianceReportRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ComplianceReport> getReportsByStatus(ComplianceReportStatus status, Pageable pageable) {
        return complianceReportRepository.findAllByStatus(status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ComplianceReport> getReportsByType(ComplianceReportType reportType, Pageable pageable) {
        return complianceReportRepository.findAllByReportType(reportType, pageable);
    }

    @Override
    public ComplianceReport generateReport(String reportId, String generatedBy) {
        ComplianceReport report = getReportById(reportId);
        validateStatus(report, ComplianceReportStatus.DRAFT, "generate");
        report.setGeneratedBy(generatedBy);
        report.setGeneratedAt(LocalDateTime.now());
        report.setStatus(ComplianceReportStatus.PENDING_REVIEW);
        report.setUpdatedOn(LocalDateTime.now());
        return complianceReportRepository.save(report);
    }

    @Override
    public ComplianceReport submitForReview(String reportId, String reviewedBy, String reviewNote) {
        ComplianceReport report = getReportById(reportId);
        report.setReviewedBy(reviewedBy);
        report.setReviewedAt(LocalDateTime.now());
        report.setReviewNote(reviewNote);
        report.setStatus(ComplianceReportStatus.REVIEWED);
        report.setUpdatedOn(LocalDateTime.now());
        return complianceReportRepository.save(report);
    }

    @Override
    public ComplianceReport approveReport(String reportId, String approvedBy) {
        ComplianceReport report = getReportById(reportId);
        validateStatus(report, ComplianceReportStatus.REVIEWED, "approve");
        report.setApprovedBy(approvedBy);
        report.setApprovedAt(LocalDateTime.now());
        report.setStatus(ComplianceReportStatus.APPROVED);
        report.setUpdatedOn(LocalDateTime.now());
        return complianceReportRepository.save(report);
    }

    @Override
    public ComplianceReport rejectReport(String reportId, String rejectedBy, String rejectionReason) {
        ComplianceReport report = getReportById(reportId);
        report.setRejectionReason(rejectionReason);
        report.setStatus(ComplianceReportStatus.REJECTED);
        report.setUpdatedOn(LocalDateTime.now());
        return complianceReportRepository.save(report);
    }

    @Override
    public ComplianceReport submitReport(String reportId, String submittedBy,
                                         String submissionReference) {
        ComplianceReport report = getReportById(reportId);
        validateStatus(report, ComplianceReportStatus.APPROVED, "submit");
        report.setSubmittedBy(submittedBy);
        report.setSubmittedAt(LocalDateTime.now());
        report.setSubmissionReference(submissionReference);
        report.setStatus(ComplianceReportStatus.SUBMITTED);
        report.setUpdatedOn(LocalDateTime.now());
        return complianceReportRepository.save(report);
    }

    @Override
    public ComplianceReport archiveReport(String reportId) {
        ComplianceReport report = getReportById(reportId);
        report.setStatus(ComplianceReportStatus.ARCHIVED);
        report.setUpdatedOn(LocalDateTime.now());
        return complianceReportRepository.save(report);
    }

    @Override
    public ComplianceReport updateReportData(String reportId, String reportDataJson,
                                             int sarCount, int flaggedTransactionCount,
                                             String fileUrl, Long fileSizeBytes) {
        ComplianceReport report = getReportById(reportId);
        report.setReportData(reportDataJson);
        report.setSarCount(sarCount);
        report.setFlaggedTransactionCount(flaggedTransactionCount);
        report.setFileUrl(fileUrl);
        report.setFileSizeBytes(fileSizeBytes);
        report.setUpdatedOn(LocalDateTime.now());
        return complianceReportRepository.save(report);
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private String generateReportReference() {
        String year = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"));
        long seq = complianceReportRepository.count() + reportCounter.incrementAndGet();
        return String.format("CR-%s-%05d", year, seq);
    }

    private void validateStatus(ComplianceReport report,
                                ComplianceReportStatus requiredStatus,
                                String action) {
        if (report.getStatus() != requiredStatus) {
            throw new ConflictException(
                    "Report must be in " + requiredStatus + " status to " + action
                    + ". Current status: " + report.getStatus());
        }
    }
}