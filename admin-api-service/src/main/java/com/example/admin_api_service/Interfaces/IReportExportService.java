package com.example.admin_api_service.Interfaces;

import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.admin_api_service.enums.ReportExportStatus;
import com.example.admin_api_service.enums.ReportFormat;
import com.example.admin_api_service.enums.ScheduledReportType;
import com.example.admin_api_service.models.reporting.ReportExport;
import com.example.admin_api_service.models.reporting.ScheduledReport;

public interface IReportExportService {
    // Trigger export from a scheduled report definition
    ReportExport generateExport(ScheduledReport scheduledReport, boolean isManual, String requestedBy);
 
    // Trigger an ad-hoc export without a scheduled report backing it
    ReportExport generateAdHocExport(ScheduledReportType reportType, ReportFormat format,
                                     LocalDateTime periodStart, LocalDateTime periodEnd,
                                     String filterParametersJson, String requestedBy);
 
    ReportExport getExportById(String exportId);
 
    Page<ReportExport> getAllExports(Pageable pageable);
 
    Page<ReportExport> getExportsByScheduledReport(String scheduledReportId, Pageable pageable);
 
    Page<ReportExport> getExportsByStatus(ReportExportStatus status, Pageable pageable);
 
    Page<ReportExport> getExportsByType(ScheduledReportType type, Pageable pageable);
 
    // Mark export as completed and attach file metadata
    ReportExport markCompleted(String exportId, String fileUrl, String fileName,
                               Long fileSizeBytes, String fileChecksum,
                               Long rowCount, Long generationDurationMs);
 
    ReportExport markFailed(String exportId, String failureReason);
 
    // Generate a fresh signed download URL
    String generateDownloadUrl(String exportId, int expiryMinutes);
 
    // Purge expired export files per retention policy
    void purgeExpiredExports();
}
