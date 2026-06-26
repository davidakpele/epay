package com.example.admin_api_service.components;

import java.time.LocalDateTime;
import com.example.admin_api_service.models.LiquidityThresholdAlert;

public record LiquidityAlertResolvedEvent(
        LiquidityThresholdAlert alert,
        Long resolvedByAdminId,
        LocalDateTime occurredAt
) {
    public LiquidityAlertResolvedEvent(LiquidityThresholdAlert alert, Long resolvedByAdminId) {
        this(alert, resolvedByAdminId, LocalDateTime.now());
    }
}