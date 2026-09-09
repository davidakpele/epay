package com.epay.domain.maintenance.entity;

import com.epay.domain.maintenance.enums.FeeType;
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
@Table(
    name = "maintenance_fee_config",
    indexes = {
        @Index(name = "idx_mfc_currency_active", columnList = "currency_code, is_active")
    }
)
public class MaintenanceFeeConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mfc_seq")
    @SequenceGenerator(name = "mfc_seq", sequenceName = "maintenance_fee_config_seq", allocationSize = 1)
    private Long id;

    /**
     * ISO-4217 currency code this config applies to (e.g. "USD", "NGN").
     */
    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    /**
     * FIXED → use feeAmount.
     * PERCENTAGE → use feePercentage (0–100); clamp with minimumFee / maximumFee.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "fee_type", nullable = false, length = 20)
    private FeeType feeType;

    /**
     * Flat fee amount for FIXED type. Null for PERCENTAGE.
     */
    @Column(name = "fee_amount", precision = 19, scale = 4)
    private BigDecimal feeAmount;

    /**
     * Percentage (e.g. 0.50 means 0.50 % of monthly volume). For PERCENTAGE type only.
     */
    @Column(name = "fee_percentage", precision = 5, scale = 2)
    private BigDecimal feePercentage;

    /**
     * Minimum charge enforced even when PERCENTAGE result is lower.
     */
    @Column(name = "minimum_fee", precision = 19, scale = 4)
    private BigDecimal minimumFee;

    /**
     * Maximum charge cap for PERCENTAGE type. Null = no cap.
     */
    @Column(name = "maximum_fee", precision = 19, scale = 4)
    private BigDecimal maximumFee;

    /**
     * Only one active config should exist per currency at a time.
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "created_by_admin_id")
    private Long createdByAdminId;

    @Column(name = "updated_by_admin_id")
    private Long updatedByAdminId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version;
}
