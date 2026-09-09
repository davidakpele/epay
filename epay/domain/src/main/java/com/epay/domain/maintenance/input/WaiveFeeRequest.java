package com.epay.domain.maintenance.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaiveFeeRequest {

    @NotNull(message = "Fee transaction ID is required")
    private Long feeTransactionId;

    @NotBlank(message = "Reason is required")
    private String reason;
}
