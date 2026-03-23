package com.example.admin_api_service.repository;

import java.util.List;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.admin_api_service.enums.ScheduledReportStatus;
import com.example.admin_api_service.enums.ScheduledReportType;
import com.example.admin_api_service.models.reporting.ScheduledReport;

@Repository
public interface ScheduledReportRepository extends JpaRepository<ScheduledReport, String> {
    Page<ScheduledReport> findAllByStatus(ScheduledReportStatus status, Pageable pageable);
    List<ScheduledReport> findAllByStatus(ScheduledReportStatus status);
    List<ScheduledReport> findAllByReportType(ScheduledReportType reportType);
 
    @Query("SELECT r FROM ScheduledReport r " +
           "WHERE r.status = :status " +
           "AND r.nextRunAt <= :now")
    List<ScheduledReport> findAllByStatusAndNextRunAtBeforeOrEqual(
            @Param("status") ScheduledReportStatus status,
            @Param("now") LocalDateTime now);
}
