package com.epay.wallet.domain.entity;

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

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "wallet_balances", joinColumns = @JoinColumn(name = "wallet_id"))
    @Builder.Default
    private List<CurrencyBalance> balances = new ArrayList<>();

    /** Whether this wallet is active and can perform transactions. */
    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    /** Whether the transaction PIN has been set by the user. */
    @Column(name = "pin_set", nullable = false)
    @Builder.Default
    private boolean pinSet = false;

    /** Bcrypt-hashed transaction PIN — never returned in responses. */
    @Column(name = "transaction_pin")
    private String transactionPin;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    // -------------------------------------------------------------------------
    // Convenience methods
    // -------------------------------------------------------------------------

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

    public void addCurrency(CurrencyBalance balance) {
        if (this.balances == null) this.balances = new ArrayList<>();
        this.balances.add(balance);
    }

    public Optional<CurrencyBalance> getDefaultBalance() {
        return balances.stream().filter(CurrencyBalance::isDefault).findFirst();
    }
}
