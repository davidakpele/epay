package com.epay.domain.investment.input;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WithdrawTargetSavingsRequest {

    @NotNull
    private Long userId;

    @NotNull
    private Long walletId;
}