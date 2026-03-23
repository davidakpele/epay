package com.example.admin_api_service.services;

import com.example.admin_api_service.Interfaces.IReportExportService;
import com.example.admin_api_service.enums.ReportExportStatus;
import com.example.admin_api_service.enums.ReportFormat;
import com.example.admin_api_service.enums.ScheduledReportType;
import com.example.admin_api_service.exceptions.BadRequestException;
import com.example.admin_api_service.exceptions.ResourceNotFoundException;
import com.example.admin_api_service.models.reporting.ReportExport;
import com.example.admin_api_service.models.reporting.ScheduledReport;
import com.example.admin_api_service.repository.ReportExportRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ReportExportServiceImpl implements IReportExportService {

    private final ReportExportRepository exportRepository;

    public ReportExportServiceImpl(ReportExportRepository exportRepository) {
        this.exportRepository = exportRepository;
    }

    @Override
    public ReportExport generateExport(ScheduledReport scheduledReport, boolean isManual,
                                        String requestedBy) {
        ReportExport export = new ReportExport();
        export.setScheduledReportId(scheduledReport.getId());
        export.setReportType(scheduledReport.getReportType());
        export.setFormat(scheduledReport.getFormat());
        export.setFilterParameters(scheduledReport.getFilterParameters());
        export.setManual(isManual);
        export.setRequestedBy(requestedBy);
        export.setStatus(ReportExportStatus.PENDING);
        export.setStartedAt(LocalDateTime.now());

        // Set retention expiry if configured
        if (scheduledReport.getRetentionDays() != null) {
            export.setExpiresAt(LocalDateTime.now().plusDays(scheduledReport.getRetentionDays()));
        }

        // Derive period from report schedule — last completed period
        LocalDateTime now = LocalDateTime.now();
        export.setPeriodEnd(now);
        export.setPeriodStart(switch (scheduledReport.getFrequency()) {
            case HOURLY    -> now.minusHours(1);
            case DAILY     -> now.minusDays(1);
            case WEEKLY    -> now.minusWeeks(1);
            case BI_WEEKLY -> now.minusWeeks(2);
            case MONTHLY   -> now.minusMonths(1);
            case QUARTERLY -> now.minusMonths(3);
            case ANNUALLY  -> now.minusYears(1);
            case CUSTOM    -> now.minusDays(1);
        });

        ReportExport saved = exportRepository.save(export);

        // In production, publish to a report generation worker queue (Kafka / RabbitMQ)
        // The worker picks up PENDING exports, generates the file, then calls markCompleted()
        saved.setStatus(ReportExportStatus.GENERATING);
        return exportRepository.save(saved);
    }

    @Override
    public ReportExport generateAdHocExport(ScheduledReportType reportType, ReportFormat format,
                                             LocalDateTime periodStart, LocalDateTime periodEnd,
                                             String filterParametersJson, String requestedBy) {
        ReportExport export = new ReportExport();
        export.setReportType(reportType);
        export.setFormat(format);
        export.setPeriodStart(periodStart);
        export.setPeriodEnd(periodEnd);
        export.setFilterParameters(filterParametersJson);
        export.setManual(true);
        export.setRequestedBy(requestedBy);
        export.setStatus(ReportExportStatus.GENERATING);
        export.setStartedAt(LocalDateTime.now());
        return exportRepository.save(export);
    }

    @Override
    @Transactional(readOnly = true)
    public ReportExport getExportById(String exportId) {
        return exportRepository.findById(exportId)
                .orElseThrow(() -> new ResourceNotFoundException("ReportExport", "id", exportId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReportExport> getAllExports(Pageable pageable) {
        return exportRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReportExport> getExportsByScheduledReport(String scheduledReportId, Pageable pageable) {
        return exportRepository.findAllByScheduledReportId(scheduledReportId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReportExport> getExportsByStatus(ReportExportStatus status, Pageable pageable) {
        return exportRepository.findAllByStatus(status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReportExport> getExportsByType(ScheduledReportType type, Pageable pageable) {
        return exportRepository.findAllByReportType(type, pageable);
    }

    @Override
    public ReportExport markCompleted(String exportId, String fileUrl, String fileName,
                                      Long fileSizeBytes, String fileChecksum,
                                      Long rowCount, Long generationDurationMs) {
        ReportExport export = getExportById(exportId);
        export.setStatus(ReportExportStatus.COMPLETED);
        export.setFileUrl(fileUrl);
        export.setFileName(fileName);
        export.setFileSizeBytes(fileSizeBytes);
        export.setFileChecksum(fileChecksum);
        export.setRowCount(rowCount);
        export.setGenerationDurationMs(generationDurationMs);
        export.setCompletedAt(LocalDateTime.now());
        export.setUpdatedOn(LocalDateTime.now());
        return exportRepository.save(export);
    }

    @Override
    public ReportExport markFailed(String exportId, String failureReason) {
        ReportExport export = getExportById(exportId);
        export.setStatus(ReportExportStatus.FAILED);
        export.setFailureReason(failureReason);
        export.setUpdatedOn(LocalDateTime.now());
        return exportRepository.save(export);
    }

    @Override
    public String generateDownloadUrl(String exportId, int expiryMinutes) {
        ReportExport export = getExportById(exportId);
        if (export.getStatus() != ReportExportStatus.COMPLETED) {
            throw new BadRequestException("Export is not ready for download. Status: " + export.getStatus());
        }
        if (export.getFileUrl() == null) {
            throw new BadRequestException("No file URL found for this export");
        }

        // In production, generate a signed URL from S3/GCS using the fileUrl as the object key
        // This is a stub — replace with your cloud storage SDK call
        String signedUrl = export.getFileUrl() + "?token=" + UUID.randomUUID()
                + "&expires=" + LocalDateTime.now().plusMinutes(expiryMinutes);

        export.setDownloadUrl(signedUrl);
        export.setDownloadUrlExpiresAt(LocalDateTime.now().plusMinutes(expiryMinutes));
        export.setUpdatedOn(LocalDateTime.now());
        exportRepository.save(export);
        return signedUrl;
    }

    @Override
    @Scheduled(cron = "0 0 3 * * *") // 3AM daily
    public void purgeExpiredExports() {
        List<ReportExport> expired = exportRepository
                .findAllByStatusNotAndExpiresAtBefore(
                        ReportExportStatus.EXPIRED, LocalDateTime.now());
        expired.forEach(export -> {
            export.setStatus(ReportExportStatus.EXPIRED);
            export.setFileUrl(null);
            export.setDownloadUrl(null);
            export.setUpdatedOn(LocalDateTime.now());
            // In production, delete the file from storage here
        });
        if (!expired.isEmpty()) {
            exportRepository.saveAll(expired);
        }
    }
}
