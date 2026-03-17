package com.example.admin_api_service.components;

import java.time.LocalDateTime;

import com.example.admin_api_service.models.LiquidityThresholdAlert;

public record LiquidityAlertTriggeredEvent(
        LiquidityThresholdAlert alert,
        LocalDateTime occurredAt
) {
    public LiquidityAlertTriggeredEvent(LiquidityThresholdAlert alert) {
        this(alert, LocalDateTime.now());
    }
}
