package com.epay.domain.beneficiary.input;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;


@Data
public class DeleteBeneficiariesRequest {

    @NotNull(message = "userId is required")
    private Long userId;

    @NotEmpty(message = "Provide at least one beneficiary ID")
    private List<Long> ids;
}
