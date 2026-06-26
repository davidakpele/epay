package com.example.admin_api_service.models.reporting;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.admin_api_service.enums.MetricCategory;
import com.example.admin_api_service.enums.MetricGranularity;

@Entity
@Table(
    name = "dashboard_metric_snapshots",
    uniqueConstraints = @UniqueConstraint(columnNames = {"metric_key", "granularity", "snapshot_at", "dimension_key"})
)
public class DashboardMetricSnapshot {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id = UUID.randomUUID().toString();

    // Unique metric identifier e.g. "total_transaction_volume", "active_users", "failed_kyc_count"
    @Column(name = "metric_key", length = 100, nullable = false)
    private String metricKey;

    @Column(name = "metric_name", length = 200, nullable = false)
    private String metricName;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 30, nullable = false)
    private MetricCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "granularity", length = 20, nullable = false)
    private MetricGranularity granularity;

    @Column(name = "dimension_key", length = 50)
    private String dimensionKey;

    // Value label for the dimension e.g. "NGN", "MOBILE", "TIER_2"
    @Column(name = "dimension_value", length = 100)
    private String dimensionValue;

    // Primary numeric value of the metric
    @Column(name = "value", precision = 24, scale = 4, nullable = false)
    private BigDecimal value;

    // Secondary numeric value — used for comparison metrics e.g. previous period value
    @Column(name = "previous_value", precision = 24, scale = 4)
    private BigDecimal previousValue;

    // Percentage change vs previous period — computed at snapshot time
    @Column(name = "change_percentage", precision = 8, scale = 4)
    private BigDecimal changePercentage;

    // Count-based value alongside the amount value (e.g. transaction count + volume)
    @Column(name = "count_value")
    private Long countValue;

    // Currency the value is denominated in — null for non-monetary metrics
    @Column(name = "currency", length = 10)
    private String currency;

    // Unit of measurement e.g. "NGN", "count", "ms", "percent"
    @Column(name = "unit", length = 20)
    private String unit;

    // Extended breakdown stored as JSON for multi-dimension charts
    // e.g. {"byChannel": {"WEB": 1200, "MOBILE": 3400}, "byStatus": {"SUCCESS": 4400, "FAILED": 200}}
    @Column(name = "breakdown", columnDefinition = "json")
    private String breakdown;

    // When this snapshot covers — start of the period
    @Column(name = "period_start", nullable = false)
    private LocalDateTime periodStart;

    // When this snapshot covers — end of the period
    @Column(name = "period_end", nullable = false)
    private LocalDateTime periodEnd;

    // Exact time this snapshot was computed
    @Column(name = "snapshot_at", nullable = false)
    private LocalDateTime snapshotAt;

    // Service or job that computed this snapshot
    @Column(name = "computed_by", length = 100)
    private String computedBy;

    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn = LocalDateTime.now();

    public DashboardMetricSnapshot() {
    }

    public DashboardMetricSnapshot(String id, String metricKey, String metricName, MetricCategory category, MetricGranularity granularity, String dimensionKey, String dimensionValue, BigDecimal value, BigDecimal previousValue, BigDecimal changePercentage, Long countValue, String currency, String unit, String breakdown, LocalDateTime periodStart, LocalDateTime periodEnd, LocalDateTime snapshotAt, String computedBy, LocalDateTime createdOn) {
        this.id = id;
        this.metricKey = metricKey;
        this.metricName = metricName;
        this.category = category;
        this.granularity = granularity;
        this.dimensionKey = dimensionKey;
        this.dimensionValue = dimensionValue;
        this.value = value;
        this.previousValue = previousValue;
        this.changePercentage = changePercentage;
        this.countValue = countValue;
        this.currency = currency;
        this.unit = unit;
        this.breakdown = breakdown;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.snapshotAt = snapshotAt;
        this.computedBy = computedBy;
        this.createdOn = createdOn;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMetricKey() { return metricKey; }
    public void setMetricKey(String metricKey) { this.metricKey = metricKey; }

    public String getMetricName() { return metricName; }
    public void setMetricName(String metricName) { this.metricName = metricName; }

    public MetricCategory getCategory() { return category; }
    public void setCategory(MetricCategory category) { this.category = category; }

    public MetricGranularity getGranularity() { return granularity; }
    public void setGranularity(MetricGranularity granularity) { this.granularity = granularity; }

    public String getDimensionKey() { return dimensionKey; }
    public void setDimensionKey(String dimensionKey) { this.dimensionKey = dimensionKey; }

    public String getDimensionValue() { return dimensionValue; }
    public void setDimensionValue(String dimensionValue) { this.dimensionValue = dimensionValue; }

    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }

    public BigDecimal getPreviousValue() { return previousValue; }
    public void setPreviousValue(BigDecimal previousValue) { this.previousValue = previousValue; }

    public BigDecimal getChangePercentage() { return changePercentage; }
    public void setChangePercentage(BigDecimal changePercentage) { this.changePercentage = changePercentage; }

    public Long getCountValue() { return countValue; }
    public void setCountValue(Long countValue) { this.countValue = countValue; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getBreakdown() { return breakdown; }
    public void setBreakdown(String breakdown) { this.breakdown = breakdown; }

    public LocalDateTime getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDateTime periodStart) { this.periodStart = periodStart; }

    public LocalDateTime getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDateTime periodEnd) { this.periodEnd = periodEnd; }

    public LocalDateTime getSnapshotAt() { return snapshotAt; }
    public void setSnapshotAt(LocalDateTime snapshotAt) { this.snapshotAt = snapshotAt; }

    public String getComputedBy() { return computedBy; }
    public void setComputedBy(String computedBy) { this.computedBy = computedBy; }

    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }
}