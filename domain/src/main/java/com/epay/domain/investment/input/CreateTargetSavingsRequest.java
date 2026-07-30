package com.epay.domain.investment.input;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateTargetSavingsRequest {

    @NotNull
    private Long userId;

    @NotNull
    private Long walletId;

    @NotNull
    @Size(max = 10)
    private String currencyCode = "NGN";

    @NotBlank
    @Size(max = 100)
    private String goalName;

    @Size(max = 500)
    private String description;

    @NotNull
    @DecimalMin(value = "0.01", message = "Target amount must be greater than 0")
    private BigDecimal targetAmount;

    private LocalDateTime targetDate;

    @Size(max = 10)
    private String goalIcon;
}
