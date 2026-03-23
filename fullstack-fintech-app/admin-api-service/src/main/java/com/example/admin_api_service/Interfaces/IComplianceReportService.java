package com.example.admin_api_service.Interfaces;

import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.admin_api_service.enums.ComplianceReportFormat;
import com.example.admin_api_service.enums.ComplianceReportStatus;
import com.example.admin_api_service.enums.ComplianceReportType;
import com.example.admin_api_service.models.complianceAndRisk.ComplianceReport;

public interface IComplianceReportService {
    ComplianceReport createReport(ComplianceReportType reportType, String title,
                                  LocalDate periodStart, LocalDate periodEnd,
                                  String regulatoryBody, ComplianceReportFormat format,
                                  LocalDate dueDate, String createdBy);
 
    ComplianceReport getReportById(String reportId);
 
    ComplianceReport getReportByReference(String reportReference);
 
    Page<ComplianceReport> getAllReports(Pageable pageable);
 
    Page<ComplianceReport> getReportsByStatus(ComplianceReportStatus status, Pageable pageable);
 
    Page<ComplianceReport> getReportsByType(ComplianceReportType reportType, Pageable pageable);
 
    ComplianceReport generateReport(String reportId, String generatedBy);
 
    ComplianceReport submitForReview(String reportId, String reviewedBy, String reviewNote);
 
    ComplianceReport approveReport(String reportId, String approvedBy);
 
    ComplianceReport rejectReport(String reportId, String rejectedBy, String rejectionReason);
 
    ComplianceReport submitReport(String reportId, String submittedBy,
                                  String submissionReference);
 
    ComplianceReport archiveReport(String reportId);
 
    ComplianceReport updateReportData(String reportId, String reportDataJson,
                                      int sarCount, int flaggedTransactionCount,
                                      String fileUrl, Long fileSizeBytes);
}
