package com.epay.domain.wallet.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class CurrencyBalance {

    @Column(name = "currency_code", nullable = false, length = 10)
    private String currencyCode;

    @Column(name = "currency_symbol", nullable = false, length = 10)
    private String currencySymbol;

    @Column(name = "balance", nullable = false, precision = 20, scale = 8)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private boolean isDefault = false;
}
