package com.example.admin_api_service.responses;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

public record DashboardSummaryResponse(

        long totalUsers,
        long totalHistory,
        long totalVirtualCards,

        List<SystemWalletResponse> wallets,

        @JsonSerialize(using = ToStringSerializer.class)
        BigDecimal platformTotalBalance,

        long totalActiveAlerts,
        boolean anyWalletBelowThreshold,
        LocalDateTime generatedAt

) {}
