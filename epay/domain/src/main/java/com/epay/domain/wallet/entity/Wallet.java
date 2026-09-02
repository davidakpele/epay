package com.epay.domain.wallet.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "wallets", indexes = {
        @Index(name = "idx_wallet_user_id", columnList = "user_id", unique = true)
})
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "wallet_seq")
    @SequenceGenerator(name = "wallet_seq", sequenceName = "wallet_sequence", allocationSize = 1)
    private Long id;

    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId;

    /**
     * Previously @ElementCollection with @Embeddable CurrencyBalance.
     * That caused Hibernate to DELETE all rows + re-INSERT all of them
     * on every balance change (e.g. 10 currencies = 1 DELETE + 10 INSERTs
     * for updating a single balance).
     *
     * Now a proper @OneToMany so Hibernate can issue a targeted
     * UPDATE wallet_balances SET balance = ? WHERE id = ?
     * for a single currency row.
     */
    @OneToMany(mappedBy = "wallet",
               cascade = CascadeType.ALL,
               orphanRemoval = true,
               fetch = FetchType.EAGER)
    @Builder.Default
    private List<CurrencyBalance> balances = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    // ── Convenience methods ───────────────────────────────────────────────────

    public Optional<CurrencyBalance> getBalance(String currencyCode) {
        return balances.stream()
                .filter(b -> b.getCurrencyCode().equalsIgnoreCase(currencyCode))
                .findFirst();
    }

    public BigDecimal getBalanceAmount(String currencyCode) {
        return getBalance(currencyCode)
                .map(CurrencyBalance::getBalance)
                .orElse(BigDecimal.ZERO);
    }

    public boolean hasCurrency(String currencyCode) {
        return balances.stream()
                .anyMatch(b -> b.getCurrencyCode().equalsIgnoreCase(currencyCode));
    }

    /**
     * Adds a new currency balance.
     * The CurrencyBalance.wallet back-reference must be set by the caller
     * (or use WalletService.addCurrencyToWallet which does this correctly).
     */
    public void addCurrency(CurrencyBalance balance) {
        if (this.balances == null) this.balances = new ArrayList<>();
        balance.setWallet(this);
        this.balances.add(balance);
    }

    public Optional<CurrencyBalance> getDefaultBalance() {
        return balances.stream().filter(CurrencyBalance::isDefault).findFirst();
    }
}
