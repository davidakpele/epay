package com.example.admin_api_service.Interfaces;

import java.util.List;

import com.example.admin_api_service.models.LiquidityThresholdAlert;
import com.example.admin_api_service.models.SystemWallet;

public interface ILiquidityAlertService {
    void evaluateThreshold(SystemWallet wallet);

    void evaluateAndResolveAlerts(SystemWallet wallet, Long adminId);

    LiquidityThresholdAlert resolveAlertManually(String alertId, Long adminId);

    List<LiquidityThresholdAlert> getActiveAlerts();

    List<LiquidityThresholdAlert> getAlertsByWallet(String walletId);
}
