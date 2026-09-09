package com.epay.domain.maintenance.dto;

import com.epay.domain.maintenance.enums.FeeType;
import com.epay.domain.maintenance.enums.MaintenanceFeeStatus;
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
public class MaintenanceFeeTransactionDto {

    private Long id;
    private Long userId;
    private String currencyCode;
    private LocalDate monthYear;
    private BigDecimal feeAmount;
    private FeeType feeType;
    private MaintenanceFeeStatus status;
    private BigDecimal deductedAmount;
    private BigDecimal walletBalanceBefore;
    private BigDecimal walletBalanceAfter;
    private BigDecimal debtAmount;
    private boolean repaid;
    private LocalDateTime repaidAt;
    private String referenceId;
    private String adminNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
