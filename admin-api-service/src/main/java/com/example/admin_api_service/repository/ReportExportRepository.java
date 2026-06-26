package com.example.admin_api_service.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.admin_api_service.enums.ReportExportStatus;
import com.example.admin_api_service.enums.ScheduledReportType;
import com.example.admin_api_service.models.reporting.ReportExport;

@Repository
public interface ReportExportRepository extends JpaRepository<ReportExport, String>  {
    Page<ReportExport> findAllByScheduledReportId(String scheduledReportId, Pageable pageable);
    Page<ReportExport> findAllByStatus(ReportExportStatus status, Pageable pageable);
    Page<ReportExport> findAllByReportType(ScheduledReportType reportType, Pageable pageable);
 
    // Find all non-expired exports whose expiresAt has passed — for purge job
    List<ReportExport> findAllByStatusNotAndExpiresAtBefore(
            ReportExportStatus excludeStatus, LocalDateTime now);
}