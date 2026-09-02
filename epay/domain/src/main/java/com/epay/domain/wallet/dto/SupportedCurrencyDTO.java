package com.epay.domain.wallet.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SupportedCurrencyDTO {

    private Long id;
    private String code;
    private String name;
    private String symbol;
    private BigDecimal exchangeRate;
    private int decimalPlaces;
    private BigDecimal minDeposit;
    private BigDecimal minWithdrawal;
    private boolean active;
    private boolean defaultEligible;
    private String countryCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
