package com.epay.domain.maintenance.input;

import com.epay.domain.maintenance.enums.FeeType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateFeeConfigRequest {

    @NotBlank(message = "Currency code is required")
    @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
    private String currencyCode;

    @NotNull(message = "Fee type is required")
    private FeeType feeType;

    @DecimalMin(value = "0.0001", message = "Fee amount must be positive")
    private BigDecimal feeAmount;

    @DecimalMin(value = "0.01", message = "Fee percentage must be at least 0.01")
    @DecimalMax(value = "100.00", message = "Fee percentage cannot exceed 100")
    private BigDecimal feePercentage;

    @DecimalMin(value = "0.0001", message = "Minimum fee must be positive")
    private BigDecimal minimumFee;

    @DecimalMin(value = "0.0001", message = "Maximum fee must be positive")
    private BigDecimal maximumFee;
}
