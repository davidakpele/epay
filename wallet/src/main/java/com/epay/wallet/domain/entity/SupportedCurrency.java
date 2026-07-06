package com.epay.wallet.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Platform currency catalog — managed exclusively by admins.
 * Defines which currencies wallets can hold.
 * Replaces the old hardcoded Currency enum.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "supported_currencies", indexes = {
        @Index(name = "idx_currency_code", columnList = "code", unique = true),
        @Index(name = "idx_currency_active", columnList = "active")
})
public class SupportedCurrency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ISO 4217 currency code — e.g. USD, EUR, NGN. Always stored uppercase. */
    @Column(nullable = false, unique = true, length = 10)
    private String code;

    /** Human-readable name — e.g. "US Dollar", "Nigerian Naira". */
    @Column(nullable = false, length = 100)
    private String name;

    /** Currency symbol — e.g. $, €, ₦. */
    @Column(nullable = false, length = 10)
    private String symbol;

    /**
     * Exchange rate relative to the platform's base currency (e.g. USD).
     * Used for swap calculations. Updated by admin or a scheduled rate-feed job.
     */
    @Column(name = "exchange_rate", nullable = false, precision = 20, scale = 8)
    private BigDecimal exchangeRate;

    /** Decimal precision for this currency — e.g. 2 for USD, 0 for JPY. */
    @Column(nullable = false)
    @Builder.Default
    private int decimalPlaces = 2;

    /** Minimum deposit amount in this currency. */
    @Column(name = "min_deposit", precision = 20, scale = 8)
    private BigDecimal minDeposit;

    /** Minimum withdrawal amount in this currency. */
    @Column(name = "min_withdrawal", precision = 20, scale = 8)
    private BigDecimal minWithdrawal;

    /** Whether this currency is currently available for wallet operations. */
    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    /** Whether this currency can be used as the default wallet currency. */
    @Column(name = "default_eligible", nullable = false)
    @Builder.Default
    private boolean defaultEligible = false;

    /** ISO 3166-1 alpha-2 country code this currency primarily belongs to. */
    @Column(name = "country_code", length = 3)
    private String countryCode;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** ID of the admin user who last updated this record. */
    @Column(name = "last_updated_by")
    private Long lastUpdatedBy;

    @Version
    private Long version;
}
