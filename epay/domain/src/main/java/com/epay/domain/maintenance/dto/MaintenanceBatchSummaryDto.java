package com.epay.domain.maintenance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceBatchSummaryDto {

    private String batchId;
    private LocalDate billingMonth;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    private int totalUsersProcessed;
    private int totalCurrenciesCharged;

    private int successfulDeductions;
    private int partialDeductions;
    private int newDebtsCreated;
    private int failedCharges;
    private int skippedAlreadyProcessed;

    private BigDecimal totalAmountDeducted;

    private BigDecimal totalDebtCreated;
}
