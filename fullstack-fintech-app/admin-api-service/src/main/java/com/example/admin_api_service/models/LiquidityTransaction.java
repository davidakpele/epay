package com.example.admin_api_service.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import com.example.admin_api_service.enums.LiquidityTransactionType;
import com.example.admin_api_service.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "liquidity_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiquidityTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // ── relationship ─────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "system_wallet_id", nullable = false)
    private SystemWallet systemWallet;

    // ── transaction details ──────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private LiquidityTransactionType type;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    /**
     * Balance BEFORE transaction
     */
    @Column(name = "balance_before", nullable = false, precision = 19, scale = 4)
    private BigDecimal balanceBefore;

    /**
     * Balance AFTER transaction
     */
    @Column(name = "balance_after", nullable = false, precision = 19, scale = 4)
    private BigDecimal balanceAfter;

     @Column(name = "initiated_by_admin_id")
    private Long initiatedByAdminId;

    @Column(name = "reserved_balance_after", precision = 19, scale = 4)
    private BigDecimal reservedBalanceAfter;

    @Column(name = "reference_transaction_id")
    private String referenceTransactionId;

    @Column(name = "reference", nullable = false, unique = true)
    private String reference; 

    @Column(name = "external_reference")
    private String externalReference; // payment provider / bank ref

    @Column(name = "performed_by_admin_id")
    private Long performedByAdminId;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.SUCCESS;

    @Column(name = "failure_reason")
    private String failureReason;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}