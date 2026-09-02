package com.epay.domain.wallet.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * One row per currency per wallet in the wallet_balances table.
 *
 * Previously @Embeddable via @ElementCollection, which caused Hibernate to
 * DELETE all rows then re-INSERT all of them on every single balance change.
 * Promoted to a proper @Entity so Hibernate can issue targeted UPDATE statements.
 *
 * The table structure is unchanged — only the mapping strategy changes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "wallet_balances",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_wallet_currency",
        columnNames = {"wallet_id", "currency_code"}
    )
)
public class CurrencyBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "currency_balance_seq")
    @SequenceGenerator(name = "currency_balance_seq",
                       sequenceName = "currency_balance_sequence",
                       allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    @JsonIgnore
    private Wallet wallet;

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
