package com.example.admin_api_service.Interfaces;

import com.example.admin_api_service.enums.AnalyticsPeriod;
import com.example.admin_api_service.responses.DashboardAnalyticsResponse;

public interface IDashboardAnalyticsService {
    DashboardAnalyticsResponse getAnalytics(String token, AnalyticsPeriod period);
}
