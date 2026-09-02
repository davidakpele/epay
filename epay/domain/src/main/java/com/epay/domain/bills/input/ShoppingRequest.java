package com.epay.domain.bills.input;

import com.epay.domain.bills.enums.PaymentMethod;
import com.epay.domain.bills.enums.ShoppingProvider;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ShoppingRequest {

    @NotNull(message = "userId is required")
    private Long userId;

    private ShoppingProvider provider;

    @NotBlank(message = "orderId is required")
    @Size(min = 3, message = "orderId must be at least 3 characters")
    private String orderId;

    @NotNull(message = "paymentMethod is required")
    private PaymentMethod paymentMethod;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "100", message = "Minimum shopping payment is 100")
    private BigDecimal amount;

    @NotBlank(message = "currency is required")
    private String currency = "NGN";
}
