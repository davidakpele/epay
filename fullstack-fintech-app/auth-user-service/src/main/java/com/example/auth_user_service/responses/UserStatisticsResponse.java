package com.example.auth_user_service.responses;

import java.time.LocalDateTime;
import java.util.List;

// payload/response/UserStatisticsResponse.java
public record UserStatisticsResponse(

        long totalUsers,
        long activeUsers,
        long inactiveUsers,
        long kycPending,

        double activePercent,
        double inactivePercent,
        double newUserPercent,

        List<AnalyticsDataPoint> registrationTrend,

        String period,
        LocalDateTime generatedAt

) {
    public record AnalyticsDataPoint(String label, long count) {}

    public static UserStatisticsResponse of(
            long totalUsers,
            long activeUsers,
            long inactiveUsers,
            long kycPending,
            List<AnalyticsDataPoint> trend,
            String period
    ) {
        long newUsers = trend.isEmpty() ? 0 : trend.get(trend.size() - 1).count();

        double activePercent   = totalUsers > 0 ? Math.round((activeUsers   * 100.0 / totalUsers) * 10.0) / 10.0 : 0;
        double inactivePercent = totalUsers > 0 ? Math.round((inactiveUsers * 100.0 / totalUsers) * 10.0) / 10.0 : 0;
        double newUserPercent  = totalUsers > 0 ? Math.round((newUsers      * 100.0 / totalUsers) * 10.0) / 10.0 : 0;

        return new UserStatisticsResponse(
                totalUsers,
                activeUsers,
                inactiveUsers,
                kycPending,
                activePercent,
                inactivePercent,
                newUserPercent,
                trend,
                period,
                LocalDateTime.now()
        );
    }
}
