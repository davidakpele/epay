package com.epay.domain.liquidity.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LiquidityStatusDTO {
    private String gateway;
    private String currency;
    private BigDecimal balance;
    private BigDecimal alertThreshold;
    private BigDecimal blockThreshold;
    private boolean belowAlertThreshold;
    private boolean belowBlockThreshold;
    private boolean payoutsBlocked;
    private LocalDateTime lastSyncAt;
    private BigDecimal totalPayoutsToday;
    private BigDecimal totalDepositsToday;
}
