package com.example.admin_api_service.responses;

public record AnalyticsDataPoint(
        String label,
        long   count,
        double amount
) {}