package com.epay.domain.liquidity.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Singleton config row (id = 1) for liquidity thresholds per gateway.
 * Admin edits this via the AdminLiquidityController.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "liquidity_config")
public class LiquidityConfig {

    @Id
    private Long id; 

    @Column(nullable = false, length = 30)
    private String gateway;

    @Column(name = "alert_threshold", nullable = false, precision = 20, scale = 2)
    private BigDecimal alertThreshold;

    @Column(name = "block_threshold", nullable = false, precision = 20, scale = 2)
    private BigDecimal blockThreshold;

    @Column(nullable = false, length = 10)
    private String currency;


    @Column(name = "alert_emails", length = 500)
    private String alertEmails;

    @Column(name = "last_known_balance", precision = 20, scale = 2)
    private BigDecimal lastKnownBalance;

    @Column(name = "last_sync_at")
    private LocalDateTime lastSyncAt;

    @Column(name = "alerts_enabled", nullable = false)
    @Builder.Default
    private boolean alertsEnabled = true;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
