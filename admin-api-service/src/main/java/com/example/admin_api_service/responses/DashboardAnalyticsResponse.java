package com.example.admin_api_service.responses;

import java.time.LocalDateTime;
import java.util.List;
import com.example.admin_api_service.enums.AnalyticsPeriod;

public record DashboardAnalyticsResponse(
        AnalyticsPeriod          period,
        List<AnalyticsDataPoint> transactions,
        LocalDateTime            generatedAt
) {
    public static DashboardAnalyticsResponse of(
            AnalyticsPeriod period,
            List<AnalyticsDataPoint> transactions
    ) {
        return new DashboardAnalyticsResponse(period, transactions, LocalDateTime.now());
    }
}