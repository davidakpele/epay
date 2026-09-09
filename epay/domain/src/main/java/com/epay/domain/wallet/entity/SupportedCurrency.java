package com.epay.domain.wallet.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sc_seq")
    @SequenceGenerator(name = "sc_seq", sequenceName = "supported_currency_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 10)
    private String symbol;

    @Column(name = "exchange_rate", nullable = false, precision = 20, scale = 8)
    private BigDecimal exchangeRate;

    @Column(nullable = false)
    @Builder.Default
    private int decimalPlaces = 2;

    @Column(name = "min_deposit", precision = 20, scale = 8)
    private BigDecimal minDeposit;

    @Column(name = "min_withdrawal", precision = 20, scale = 8)
    private BigDecimal minWithdrawal;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "default_eligible", nullable = false)
    @Builder.Default
    private boolean defaultEligible = false;

    @Column(name = "country_code", length = 3)
    private String countryCode;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_updated_by")
    private Long lastUpdatedBy;

    @Version
    private Long version;
}
