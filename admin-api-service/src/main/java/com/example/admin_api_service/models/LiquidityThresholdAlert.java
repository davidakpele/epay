package com.example.admin_api_service.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import com.example.admin_api_service.enums.AlertStatus;
import com.example.admin_api_service.enums.Currency;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "liquidity_threshold_alerts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiquidityThresholdAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "system_wallet_id", nullable = false)
    private SystemWallet systemWallet;

    @Column(name = "balance_at_trigger", nullable = false, precision = 19, scale = 4)
    private BigDecimal balanceAtTrigger;

    @Column(name = "threshold_at_trigger", nullable = false, precision = 19, scale = 4)
    private BigDecimal thresholdAtTrigger;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, length = 10)
    private Currency currency;

    @Column(name = "threshold_value", nullable = false, precision = 19, scale = 4)
    private BigDecimal thresholdValue;

    @Column(name = "available_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal availableBalance;

    @Column(name = "total_liabilities", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalLiabilities;

    @Column(name = "reserve_ratio", nullable = false, precision = 10, scale = 4)
    private BigDecimal reserveRatio;

    @Column(name = "resolved_by_admin_id")
    private Long resolvedByAdminId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private AlertStatus status = AlertStatus.ACTIVE;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @CreationTimestamp
    @Column(name = "triggered_at", updatable = false)
    private LocalDateTime triggeredAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public void resolve() {
        this.status = AlertStatus.RESOLVED;
        this.resolvedAt = LocalDateTime.now();
    }
}