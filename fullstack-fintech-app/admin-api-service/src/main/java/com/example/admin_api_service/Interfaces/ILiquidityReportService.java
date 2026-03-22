package com.example.admin_api_service.Interfaces;

import com.example.admin_api_service.payloads.LiquidityStatsResponse;
import com.example.admin_api_service.enums.Currency;
import java.util.List;

public interface ILiquidityReportService{
    LiquidityStatsResponse getStats(Currency currency);
    List<LiquidityStatsResponse> getAllStats();
}
