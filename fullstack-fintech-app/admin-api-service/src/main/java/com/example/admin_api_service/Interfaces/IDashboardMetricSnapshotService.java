package com.example.admin_api_service.Interfaces;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.admin_api_service.enums.MetricCategory;
import com.example.admin_api_service.enums.MetricGranularity;
import com.example.admin_api_service.models.reporting.DashboardMetricSnapshot;

public interface IDashboardMetricSnapshotService {
    // Record a single metric snapshot
    DashboardMetricSnapshot recordSnapshot(String metricKey, String metricName,
                                           MetricCategory category, MetricGranularity granularity,
                                           String dimensionKey, String dimensionValue,
                                           BigDecimal value, BigDecimal previousValue,
                                           Long countValue, String currency, String unit,
                                           String breakdownJson, LocalDateTime periodStart,
                                           LocalDateTime periodEnd, String computedBy);
 
    DashboardMetricSnapshot getSnapshotById(String snapshotId);
 
    Page<DashboardMetricSnapshot> getAllSnapshots(Pageable pageable);
 
    // Latest snapshot for a given metric + granularity
    Optional<DashboardMetricSnapshot> getLatestSnapshot(String metricKey,
                                                         MetricGranularity granularity,
                                                         String dimensionKey);
 
    // All snapshots for a metric in a time range (for trend charts)
    List<DashboardMetricSnapshot> getSnapshotHistory(String metricKey,
                                                      MetricGranularity granularity,
                                                      String dimensionKey,
                                                      LocalDateTime from, LocalDateTime to);
 
    // All latest snapshots for a category (dashboard summary cards)
    List<DashboardMetricSnapshot> getLatestSnapshotsForCategory(MetricCategory category,
                                                                  MetricGranularity granularity);
 
    // Compute and persist all standard platform metrics for the given granularity
    void computeAllMetrics(MetricGranularity granularity);
 
    // Purge snapshots older than retentionDays
    void purgeOldSnapshots(MetricGranularity granularity, int retentionDays);
}
