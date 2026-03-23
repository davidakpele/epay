package com.example.admin_api_service.services;

import com.example.admin_api_service.Interfaces.IDashboardMetricSnapshotService;
import com.example.admin_api_service.enums.MetricCategory;
import com.example.admin_api_service.enums.MetricGranularity;
import com.example.admin_api_service.exceptions.ResourceNotFoundException;
import com.example.admin_api_service.models.reporting.DashboardMetricSnapshot;
import com.example.admin_api_service.repository.DashboardMetricSnapshotRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DashboardMetricSnapshotServiceImpl implements IDashboardMetricSnapshotService {

    // Retention: keep HOURLY for 7 days, DAILY for 180 days, everything else forever
    private static final int HOURLY_RETENTION_DAYS = 7;
    private static final int DAILY_RETENTION_DAYS = 180;

    private final DashboardMetricSnapshotRepository snapshotRepository;

    public DashboardMetricSnapshotServiceImpl(DashboardMetricSnapshotRepository snapshotRepository) {
        this.snapshotRepository = snapshotRepository;
    }

    @Override
    public DashboardMetricSnapshot recordSnapshot(String metricKey, String metricName,
                                                   MetricCategory category,
                                                   MetricGranularity granularity,
                                                   String dimensionKey, String dimensionValue,
                                                   BigDecimal value, BigDecimal previousValue,
                                                   Long countValue, String currency, String unit,
                                                   String breakdownJson,
                                                   LocalDateTime periodStart,
                                                   LocalDateTime periodEnd,
                                                   String computedBy) {
        DashboardMetricSnapshot snapshot = new DashboardMetricSnapshot();
        snapshot.setMetricKey(metricKey);
        snapshot.setMetricName(metricName);
        snapshot.setCategory(category);
        snapshot.setGranularity(granularity);
        snapshot.setDimensionKey(dimensionKey);
        snapshot.setDimensionValue(dimensionValue);
        snapshot.setValue(value != null ? value : BigDecimal.ZERO);
        snapshot.setPreviousValue(previousValue);
        snapshot.setCountValue(countValue);
        snapshot.setCurrency(currency);
        snapshot.setUnit(unit);
        snapshot.setBreakdown(breakdownJson);
        snapshot.setPeriodStart(periodStart);
        snapshot.setPeriodEnd(periodEnd);
        snapshot.setSnapshotAt(LocalDateTime.now());
        snapshot.setComputedBy(computedBy);

        // Compute change percentage
        if (previousValue != null && previousValue.compareTo(BigDecimal.ZERO) != 0
                && value != null) {
            BigDecimal change = value.subtract(previousValue)
                    .divide(previousValue.abs(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            snapshot.setChangePercentage(change);
        }

        return snapshotRepository.save(snapshot);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardMetricSnapshot getSnapshotById(String snapshotId) {
        return snapshotRepository.findById(snapshotId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "DashboardMetricSnapshot", "id", snapshotId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DashboardMetricSnapshot> getAllSnapshots(Pageable pageable) {
        return snapshotRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DashboardMetricSnapshot> getLatestSnapshot(String metricKey,
                                                                MetricGranularity granularity,
                                                                String dimensionKey) {
        return snapshotRepository
                .findTopByMetricKeyAndGranularityAndDimensionKeyOrderBySnapshotAtDesc(
                        metricKey, granularity, dimensionKey);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DashboardMetricSnapshot> getSnapshotHistory(String metricKey,
                                                             MetricGranularity granularity,
                                                             String dimensionKey,
                                                             LocalDateTime from,
                                                             LocalDateTime to) {
        return snapshotRepository
                .findAllByMetricKeyAndGranularityAndDimensionKeyAndSnapshotAtBetweenOrderBySnapshotAtAsc(
                        metricKey, granularity, dimensionKey, from, to);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DashboardMetricSnapshot> getLatestSnapshotsForCategory(MetricCategory category,
                                                                         MetricGranularity granularity) {
        return snapshotRepository.findLatestPerMetricKeyByCategory(category, granularity);
    }

    @Override
    public void computeAllMetrics(MetricGranularity granularity) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime periodStart = resolvePeriodStart(granularity, now);

        // ── Transactions ──────────────────────────────────────────────────────
        recordFromQuery("total_transaction_volume", "Total Transaction Volume",
                MetricCategory.TRANSACTIONS, granularity,
                null, null, null, "NGN", "NGN",
                periodStart, now);

        recordFromQuery("total_transaction_count", "Total Transaction Count",
                MetricCategory.TRANSACTIONS, granularity,
                null, null, null, null, "count",
                periodStart, now);

        recordFromQuery("failed_transaction_count", "Failed Transaction Count",
                MetricCategory.TRANSACTIONS, granularity,
                null, null, null, null, "count",
                periodStart, now);

        // ── Users & KYC ───────────────────────────────────────────────────────
        recordFromQuery("active_users", "Active Users",
                MetricCategory.USERS, granularity,
                null, null, null, null, "count",
                periodStart, now);

        recordFromQuery("kyc_approvals", "KYC Approvals",
                MetricCategory.KYC, granularity,
                null, null, null, null, "count",
                periodStart, now);

        recordFromQuery("kyc_rejections", "KYC Rejections",
                MetricCategory.KYC, granularity,
                null, null, null, null, "count",
                periodStart, now);

        // ── Fees & Revenue ────────────────────────────────────────────────────
        recordFromQuery("total_fee_revenue", "Total Fee Revenue",
                MetricCategory.FEES, granularity,
                null, null, null, "NGN", "NGN",
                periodStart, now);

        // ── Compliance ────────────────────────────────────────────────────────
        recordFromQuery("open_aml_alerts", "Open AML Alerts",
                MetricCategory.COMPLIANCE, granularity,
                null, null, null, null, "count",
                periodStart, now);

        recordFromQuery("open_aml_cases", "Open AML Cases",
                MetricCategory.COMPLIANCE, granularity,
                null, null, null, null, "count",
                periodStart, now);

        // ── Settlements ───────────────────────────────────────────────────────
        recordFromQuery("settlement_volume", "Settlement Volume",
                MetricCategory.SETTLEMENTS, granularity,
                null, null, null, "NGN", "NGN",
                periodStart, now);
    }

    @Override
    @Scheduled(cron = "0 0 4 * * *") // 4AM daily
    public void purgeOldSnapshots(MetricGranularity granularity, int retentionDays) {
        // Called by scheduled tasks below with appropriate retention per granularity
        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
        snapshotRepository.deleteAllByGranularityAndSnapshotAtBefore(granularity, cutoff);
    }

    // ── Scheduled compute tasks ────────────────────────────────────────────────

    @Scheduled(fixedDelay = 3600000) // every hour
    public void computeHourlyMetrics() {
        computeAllMetrics(MetricGranularity.HOURLY);
    }

    @Scheduled(cron = "0 5 0 * * *") // daily at 00:05
    public void computeDailyMetrics() {
        computeAllMetrics(MetricGranularity.DAILY);
        purgeOldSnapshots(MetricGranularity.HOURLY, HOURLY_RETENTION_DAYS);
    }

    @Scheduled(cron = "0 10 0 * * MON") // every Monday at 00:10
    public void computeWeeklyMetrics() {
        computeAllMetrics(MetricGranularity.WEEKLY);
        purgeOldSnapshots(MetricGranularity.DAILY, DAILY_RETENTION_DAYS);
    }

    @Scheduled(cron = "0 15 0 1 * *") // 1st of each month at 00:15
    public void computeMonthlyMetrics() {
        computeAllMetrics(MetricGranularity.MONTHLY);
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private LocalDateTime resolvePeriodStart(MetricGranularity granularity, LocalDateTime now) {
        return switch (granularity) {
            case MINUTE    -> now.minusMinutes(1);
            case HOURLY    -> now.minusHours(1);
            case DAILY     -> now.minusDays(1);
            case WEEKLY    -> now.minusWeeks(1);
            case MONTHLY   -> now.minusMonths(1);
            case QUARTERLY -> now.minusMonths(3);
            case ANNUALLY  -> now.minusYears(1);
            case ALL_TIME  -> LocalDateTime.of(2000, 1, 1, 0, 0);
        };
    }

    private void recordFromQuery(String metricKey, String metricName,
                                  MetricCategory category, MetricGranularity granularity,
                                  String dimensionKey, String dimensionValue,
                                  BigDecimal value, String currency, String unit,
                                  LocalDateTime periodStart, LocalDateTime periodEnd) {
        // Fetch previous snapshot for change % calculation
        BigDecimal previous = getLatestSnapshot(metricKey, granularity, dimensionKey)
                .map(DashboardMetricSnapshot::getValue).orElse(null);

        // In production, replace `value` with an actual DB aggregate query result
        // e.g. settlementRecordRepository.sumNetAmountByCurrencyAndPeriod("NGN", periodStart, periodEnd)
        BigDecimal computedValue = value != null ? value : BigDecimal.ZERO;

        recordSnapshot(metricKey, metricName, category, granularity,
                dimensionKey, dimensionValue, computedValue, previous,
                null, currency, unit, null, periodStart, periodEnd, "SCHEDULER");
    }
}