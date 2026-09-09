package com.epay.domain.maintenance.entity;

import com.epay.domain.maintenance.enums.FeeType;
import com.epay.domain.maintenance.enums.MaintenanceFeeStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "maintenance_fee_transactions",
    indexes = {
        @Index(name = "idx_mft_user_currency_month", columnList = "user_id, currency_code, month_year"),
        @Index(name = "idx_mft_status",              columnList = "status"),
        @Index(name = "idx_mft_reference",           columnList = "reference_id", unique = true)
    }
)
public class MaintenanceFeeTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mft_seq")
    @SequenceGenerator(name = "mft_seq", sequenceName = "maintenance_fee_transaction_seq", allocationSize = 1)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Column(name = "month_year", nullable = false)
    private LocalDate monthYear;

    @Column(name = "fee_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal feeAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "fee_type", nullable = false, length = 20)
    private FeeType feeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private MaintenanceFeeStatus status = MaintenanceFeeStatus.PENDING;

    @Column(name = "deducted_amount", precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal deductedAmount = BigDecimal.ZERO;

    @Column(name = "wallet_balance_before", precision = 19, scale = 4)
    private BigDecimal walletBalanceBefore;

    @Column(name = "wallet_balance_after", precision = 19, scale = 4)
    private BigDecimal walletBalanceAfter;

    @Column(name = "debt_amount", precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal debtAmount = BigDecimal.ZERO;

    @Column(name = "repaid", nullable = false)
    @Builder.Default
    private boolean repaid = false;

    @Column(name = "repaid_at")
    private LocalDateTime repaidAt;

    @Column(name = "reference_id", nullable = false, unique = true, length = 50)
    private String referenceId;

    @Column(name = "admin_notes", length = 500)
    private String adminNotes;

    @Column(name = "batch_id", length = 36)
    private String batchId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version;
}
