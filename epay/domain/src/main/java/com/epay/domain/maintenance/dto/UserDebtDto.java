package com.epay.domain.maintenance.dto;

import com.epay.domain.maintenance.enums.DebtStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDebtDto {

    private Long id;
    private Long userId;
    private String currencyCode;
    private BigDecimal totalDebt;
    private BigDecimal totalRepaid;
    private DebtStatus status;
    private LocalDateTime lastActivityDate;
    private LocalDateTime settledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
