package com.example.admin_api_service.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.example.admin_api_service.enums.Currency;
import com.example.admin_api_service.enums.WalletStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Entity
@Table(name = "system_wallets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemWallet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, unique = true, length = 10)
    private Currency currency;

    @Column(name = "balance", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "reserved_balance", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal reservedBalance = BigDecimal.ZERO;

    @Column(name = "minimum_threshold", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal minimumThreshold = BigDecimal.ZERO;

    @Column(name = "total_user_liabilities", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal totalUserLiabilities = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private WalletStatus status = WalletStatus.ACTIVE;

    @Column(name = "wallet_reference", nullable = false, unique = true)
    private String walletReference; 

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "systemWallet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LiquidityTransaction> transactions;
 
    @OneToMany(mappedBy = "systemWallet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LiquidityThresholdAlert> thresholdAlerts;

    @Column(name = "last_funded_at")
    private LocalDateTime lastFundedAt;

    @Column(name = "last_funded_by")
    private Long lastFundedByAdminId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public BigDecimal getAvailableBalance() {
        return balance.subtract(reservedBalance);
    }
    
    @Transient
    public BigDecimal getReserveRatio() {
        if (totalUserLiabilities.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ONE;
        return getAvailableBalance().divide(totalUserLiabilities, 4, java.math.RoundingMode.HALF_UP);
    }

    @Transient
    public boolean isBelowThreshold() {
        return getAvailableBalance().compareTo(minimumThreshold) <= 0;
    }

}
