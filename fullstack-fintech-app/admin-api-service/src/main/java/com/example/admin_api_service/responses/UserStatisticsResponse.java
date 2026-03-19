package com.example.admin_api_service.responses;

import java.time.LocalDateTime;
import java.util.List;

public record UserStatisticsResponse(

        long   totalUsers,
        long   activeUsers,
        long   inactiveUsers,
        long   kycPending,
        double activePercent,
        double inactivePercent,
        double newUserPercent,
        List<AnalyticsDataPoint> registrationTrend,
        String period,
        LocalDateTime generatedAt

) {
    public record AnalyticsDataPoint(String label, long count) {}
}
