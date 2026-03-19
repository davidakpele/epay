package com.example.admin_api_service.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.admin_api_service.clients.HistoryServiceClient;
import com.example.admin_api_service.enums.AnalyticsPeriod;
import com.example.admin_api_service.responses.AnalyticsDataPoint;
import com.example.admin_api_service.responses.DashboardAnalyticsResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardAnalyticsService {

    private final HistoryServiceClient historyServiceClient;

    public DashboardAnalyticsResponse getAnalytics(String token, AnalyticsPeriod period) {
        List<AnalyticsDataPoint> transactions = historyServiceClient.getTransactionAnalytics(token, period.name());

        return DashboardAnalyticsResponse.of(period, transactions);
    }
}
