package com.example.admin_api_service.Interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import com.example.admin_api_service.enums.ScheduledReportStatus;
import com.example.admin_api_service.enums.ScheduledReportType;
import com.example.admin_api_service.models.reporting.ScheduledReport;

public interface IScheduledReportService {

    ScheduledReport createReport(ScheduledReport report, String createdBy);
 
    ScheduledReport updateReport(String reportId, ScheduledReport updated, String updatedBy);
 
    ScheduledReport getReportById(String reportId);
 
    Page<ScheduledReport> getAllReports(Pageable pageable);
 
    Page<ScheduledReport> getReportsByStatus(ScheduledReportStatus status, Pageable pageable);
 
    List<ScheduledReport> getReportsByType(ScheduledReportType type);
 
    List<ScheduledReport> getActiveReports();
 
    // Returns all reports whose nextRunAt is due now
    List<ScheduledReport> getDueReports();
 
    void pauseReport(String reportId, String updatedBy);
 
    void resumeReport(String reportId, String updatedBy);
 
    void archiveReport(String reportId, String updatedBy);
 
    void deleteReport(String reportId);
 
    // Compute and persist the next run timestamp based on frequency
    ScheduledReport updateNextRunAt(String reportId, boolean lastRunSucceeded);
 
    // Trigger a manual run outside the schedule
    void triggerManualRun(String reportId, String triggeredBy);
 
    // Called by the scheduler to execute all due reports
    void processDueReports();
}
