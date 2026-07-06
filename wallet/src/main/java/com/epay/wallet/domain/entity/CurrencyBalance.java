package com.epay.wallet.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Represents a single currency balance within a wallet.
 * Embedded as a collection inside {@link Wallet}.
 *
 * The currency code must reference an active {@link SupportedCurrency}.
 * This is enforced at the service layer, not the DB layer (to avoid FK across embeddables).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class CurrencyBalance {

    /** ISO 4217 currency code — references SupportedCurrency.code. */
    @Column(name = "currency_code", nullable = false, length = 10)
    private String currencyCode;

    /** Snapshot of the symbol at the time of creation — for display without a join. */
    @Column(name = "currency_symbol", nullable = false, length = 10)
    private String currencySymbol;

    /** Current balance in this currency. Never negative. */
    @Column(name = "balance", nullable = false, precision = 20, scale = 8)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    /** Whether this is the user's primary/default currency. Only one can be true per wallet. */
    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private boolean isDefault = false;
}
