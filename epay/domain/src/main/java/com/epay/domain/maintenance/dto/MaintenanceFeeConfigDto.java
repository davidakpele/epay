package com.epay.domain.maintenance.dto;

import com.epay.domain.maintenance.enums.FeeType;
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
public class MaintenanceFeeConfigDto {

    private Long id;
    private String currencyCode;
    private FeeType feeType;
    private BigDecimal feeAmount;
    private BigDecimal feePercentage;
    private BigDecimal minimumFee;
    private BigDecimal maximumFee;
    private boolean active;
    private Long createdByAdminId;
    private Long updatedByAdminId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
