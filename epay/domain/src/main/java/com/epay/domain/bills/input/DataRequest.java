package com.epay.domain.bills.input;

import com.epay.domain.bills.enums.MobileNetwork;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;


@Data
public class DataRequest {

    @NotNull(message = "userId is required")
    private Long userId;

    @NotNull(message = "network is required")
    private MobileNetwork network;

    @NotBlank(message = "countryCode is required")
    @Pattern(regexp = "\\+\\d{1,4}", message = "countryCode must be a valid dialing code e.g. +234")
    private String countryCode;

    @NotBlank(message = "phoneNumber is required")
    @Pattern(regexp = "\\d{7,15}", message = "phoneNumber must be digits only (7–15 digits)")
    private String phoneNumber;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "50", message = "Minimum data bundle amount is 50")
    private BigDecimal amount;

    @NotBlank(message = "currency is required")
    private String currency = "NGN";
}
