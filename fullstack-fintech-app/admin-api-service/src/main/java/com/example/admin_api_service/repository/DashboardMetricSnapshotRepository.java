package com.example.admin_api_service.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.admin_api_service.enums.MetricCategory;
import com.example.admin_api_service.enums.MetricGranularity;
import com.example.admin_api_service.models.reporting.DashboardMetricSnapshot;

@Repository
public interface DashboardMetricSnapshotRepository extends JpaRepository<DashboardMetricSnapshot, String> {

    Optional<DashboardMetricSnapshot> findTopByMetricKeyAndGranularityAndDimensionKeyOrderBySnapshotAtDesc(
            String metricKey, MetricGranularity granularity, String dimensionKey);
 
    List<DashboardMetricSnapshot> findAllByMetricKeyAndGranularityAndDimensionKeyAndSnapshotAtBetweenOrderBySnapshotAtAsc(
            String metricKey, MetricGranularity granularity, String dimensionKey,
            LocalDateTime from, LocalDateTime to);

    @Query("SELECT s FROM DashboardMetricSnapshot s " +
           "WHERE s.category = :category " +
           "AND s.granularity = :granularity " +
           "AND s.snapshotAt = (" +
           "  SELECT MAX(s2.snapshotAt) FROM DashboardMetricSnapshot s2 " +
           "  WHERE s2.metricKey = s.metricKey " +
           "  AND s2.granularity = :granularity " +
           "  AND s2.dimensionKey = s.dimensionKey" +
           ")")
    List<DashboardMetricSnapshot> findLatestPerMetricKeyByCategory(
            @Param("category") MetricCategory category,
            @Param("granularity") MetricGranularity granularity);
 
    @Modifying
    @Query("DELETE FROM DashboardMetricSnapshot s " +
           "WHERE s.granularity = :granularity AND s.snapshotAt < :cutoff")
    void deleteAllByGranularityAndSnapshotAtBefore(
            @Param("granularity") MetricGranularity granularity,
            @Param("cutoff") LocalDateTime cutoff);
}