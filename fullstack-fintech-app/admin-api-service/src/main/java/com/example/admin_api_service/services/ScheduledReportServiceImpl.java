package com.example.admin_api_service.services;

import com.example.admin_api_service.Interfaces.IReportExportService;
import com.example.admin_api_service.Interfaces.IScheduledReportService;
import com.example.admin_api_service.enums.ScheduledReportStatus;
import com.example.admin_api_service.enums.ScheduledReportType;
import com.example.admin_api_service.exceptions.ConflictException;
import com.example.admin_api_service.exceptions.ResourceNotFoundException;
import com.example.admin_api_service.models.reporting.ScheduledReport;
import com.example.admin_api_service.repository.ScheduledReportRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@Transactional
public class ScheduledReportServiceImpl implements IScheduledReportService {

    private final ScheduledReportRepository reportRepository;
    private final IReportExportService reportExportService;

    public ScheduledReportServiceImpl(ScheduledReportRepository reportRepository, @Lazy IReportExportService reportExportService) {
        this.reportRepository = reportRepository;
        this.reportExportService = reportExportService;
    }

    @Override
    public ScheduledReport createReport(ScheduledReport report, String createdBy) {
        report.setCreatedBy(createdBy);
        report.setStatus(ScheduledReportStatus.ACTIVE);
        report.setTotalRuns(0);
        report.setFailedRuns(0);
        report.setNextRunAt(computeNextRunAt(report, LocalDateTime.now()));
        return reportRepository.save(report);
    }

    @Override
    public ScheduledReport updateReport(String reportId, ScheduledReport updated, String updatedBy) {
        ScheduledReport existing = getReportById(reportId);
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setReportType(updated.getReportType());
        existing.setFrequency(updated.getFrequency());
        existing.setFormat(updated.getFormat());
        existing.setCronExpression(updated.getCronExpression());
        existing.setRunAtTime(updated.getRunAtTime());
        existing.setRunOnDayOfWeek(updated.getRunOnDayOfWeek());
        existing.setRunOnDayOfMonth(updated.getRunOnDayOfMonth());
        existing.setFilterParameters(updated.getFilterParameters());
        existing.setDeliveryChannel(updated.getDeliveryChannel());
        existing.setDeliveryRecipients(updated.getDeliveryRecipients());
        existing.setRecipientAdminIds(updated.getRecipientAdminIds());
        existing.setStoragePath(updated.getStoragePath());
        existing.setRetentionDays(updated.getRetentionDays());
        existing.setUpdatedBy(updatedBy);
        existing.setUpdatedOn(LocalDateTime.now());
        existing.setNextRunAt(computeNextRunAt(existing, LocalDateTime.now()));
        return reportRepository.save(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public ScheduledReport getReportById(String reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("ScheduledReport", "id", reportId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ScheduledReport> getAllReports(Pageable pageable) {
        return reportRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ScheduledReport> getReportsByStatus(ScheduledReportStatus status, Pageable pageable) {
        return reportRepository.findAllByStatus(status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduledReport> getReportsByType(ScheduledReportType type) {
        return reportRepository.findAllByReportType(type);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduledReport> getActiveReports() {
        return reportRepository.findAllByStatus(ScheduledReportStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduledReport> getDueReports() {
        return reportRepository.findAllByStatusAndNextRunAtBeforeOrEqual(
                ScheduledReportStatus.ACTIVE, LocalDateTime.now());
    }

    @Override
    public void pauseReport(String reportId, String updatedBy) {
        ScheduledReport report = getReportById(reportId);
        report.setStatus(ScheduledReportStatus.PAUSED);
        report.setUpdatedBy(updatedBy);
        report.setUpdatedOn(LocalDateTime.now());
        reportRepository.save(report);
    }

    @Override
    public void resumeReport(String reportId, String updatedBy) {
        ScheduledReport report = getReportById(reportId);
        if (report.getStatus() != ScheduledReportStatus.PAUSED) {
            throw new ConflictException("Only PAUSED reports can be resumed");
        }
        report.setStatus(ScheduledReportStatus.ACTIVE);
        report.setNextRunAt(computeNextRunAt(report, LocalDateTime.now()));
        report.setUpdatedBy(updatedBy);
        report.setUpdatedOn(LocalDateTime.now());
        reportRepository.save(report);
    }

    @Override
    public void archiveReport(String reportId, String updatedBy) {
        ScheduledReport report = getReportById(reportId);
        report.setStatus(ScheduledReportStatus.ARCHIVED);
        report.setUpdatedBy(updatedBy);
        report.setUpdatedOn(LocalDateTime.now());
        reportRepository.save(report);
    }

    @Override
    public void deleteReport(String reportId) {
        ScheduledReport report = getReportById(reportId);
        if (report.getStatus() == ScheduledReportStatus.ACTIVE) {
            throw new ConflictException("Active reports cannot be deleted. Pause or archive first.");
        }
        reportRepository.delete(report);
    }

    @Override
    public ScheduledReport updateNextRunAt(String reportId, boolean lastRunSucceeded) {
        ScheduledReport report = getReportById(reportId);
        LocalDateTime next = computeNextRunAt(report, LocalDateTime.now());
        report.setNextRunAt(next);
        report.setLastRunAt(LocalDateTime.now());
        report.setLastRunStatus(lastRunSucceeded ? "SUCCESS" : "FAILED");
        report.setTotalRuns(report.getTotalRuns() + 1);
        if (!lastRunSucceeded) {
            report.setFailedRuns(report.getFailedRuns() + 1);
        }
        report.setUpdatedOn(LocalDateTime.now());
        return reportRepository.save(report);
    }

    @Override
    public void triggerManualRun(String reportId, String triggeredBy) {
        ScheduledReport report = getReportById(reportId);
        reportExportService.generateExport(report, true, triggeredBy);
    }

    @Override
    @Scheduled(fixedDelay = 60000) // every minute
    public void processDueReports() {
        List<ScheduledReport> due = getDueReports();
        due.forEach(report -> {
            try {
                reportExportService.generateExport(report, false, "SCHEDULER");
                updateNextRunAt(report.getId(), true);
            } catch (Exception e) {
                updateNextRunAt(report.getId(), false);
            }
        });
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private LocalDateTime computeNextRunAt(ScheduledReport report, LocalDateTime from) {
        LocalTime runTime = report.getRunAtTime() != null ? report.getRunAtTime() : LocalTime.of(6, 0);

        return switch (report.getFrequency()) {
            case HOURLY -> from.plusHours(1);
            case DAILY -> from.plusDays(1).withHour(runTime.getHour())
                    .withMinute(runTime.getMinute()).withSecond(0);
            case WEEKLY -> {
                int targetDay = report.getRunOnDayOfWeek() != null ? report.getRunOnDayOfWeek() : 1;
                LocalDateTime next = from.plusDays(1);
                while (next.getDayOfWeek().getValue() != targetDay) next = next.plusDays(1);
                yield next.withHour(runTime.getHour()).withMinute(runTime.getMinute()).withSecond(0);
            }
            case BI_WEEKLY -> from.plusWeeks(2).withHour(runTime.getHour())
                    .withMinute(runTime.getMinute()).withSecond(0);
            case MONTHLY -> {
                int dayOfMonth = report.getRunOnDayOfMonth() != null ? report.getRunOnDayOfMonth() : 1;
                LocalDateTime next = from.plusMonths(1).withDayOfMonth(
                        Math.min(dayOfMonth, from.plusMonths(1).toLocalDate().lengthOfMonth()));
                yield next.withHour(runTime.getHour()).withMinute(runTime.getMinute()).withSecond(0);
            }
            case QUARTERLY -> from.plusMonths(3).withHour(runTime.getHour())
                    .withMinute(runTime.getMinute()).withSecond(0);
            case ANNUALLY -> from.plusYears(1).withHour(runTime.getHour())
                    .withMinute(runTime.getMinute()).withSecond(0);
            case CUSTOM -> from.plusHours(24); // fallback for cron — let a cron lib handle this
        };
    }
}
