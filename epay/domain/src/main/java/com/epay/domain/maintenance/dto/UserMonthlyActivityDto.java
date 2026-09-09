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
public class UserMonthlyActivityDto {

    private Long id;
    private Long userId;
    private String currencyCode;
    private LocalDate monthYear;
    private Integer transactionCount;
    private BigDecimal totalVolume;
    private boolean hasActivity;
    private boolean processed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
