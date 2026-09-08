package com.epay.domain.history.entity;

import com.epay.domain.history.dto.StatusTimeline;
import com.epay.domain.history.enums.TransactionStatus;
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
@Table(name = "transactions", indexes = {
        @Index(name = "idx_txn_transaction_id",  columnList = "transaction_id",  unique = true),
        @Index(name = "idx_txn_reference",        columnList = "reference"),
        @Index(name = "idx_txn_user_id",          columnList = "user_id"),
        @Index(name = "idx_txn_wallet_id",        columnList = "wallet_id"),
        @Index(name = "idx_txn_current_status",   columnList = "current_status"),
        @Index(name = "idx_txn_type",             columnList = "transaction_type"),
        @Index(name = "idx_txn_created_at",       columnList = "created_at"),
        @Index(name = "idx_txn_idempotency",      columnList = "idempotency_key", unique = true),
        @Index(name = "idx_txn_publish_access",   columnList = "publish_access")
})
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "txn_seq")
    @SequenceGenerator(name = "txn_seq", sequenceName = "transaction_sequence", allocationSize = 1)
    private Long id;

    @Column(name = "transaction_id", nullable = false, unique = true, length = 50)
    private String transactionId;

    @Column(name = "reference", length = 100)
    private String reference;

    @Column(name = "idempotency_key", unique = true, length = 100)
    private String idempotencyKey;


    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "wallet_id")
    private Long walletId;

    @Column(name = "account_holder", length = 200)
    private String accountHolder;


    @Column(name = "counterparty_user_id")
    private Long counterpartyUserId;

    @Column(name = "counterparty_wallet_id")
    private Long counterpartyWalletId;

    @Column(name = "counterparty_account_holder", length = 200)
    private String counterpartyAccountHolder;

    @Column(name = "transaction_type", nullable = false, length = 50)
    private String transactionType;

    @Column(name = "debit_credit", nullable = false, length = 10)
    private String debitCredit;

    @Column(name = "channel", length = 50)
    private String channel;

    @Column(name = "gross_amount", nullable = false, precision = 20, scale = 8)
    private BigDecimal grossAmount;

    @Column(name = "fee_amount", precision = 20, scale = 8)
    @Builder.Default
    private BigDecimal feeAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 20, scale = 8)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "net_amount", precision = 20, scale = 8)
    private BigDecimal netAmount;

    @Column(name = "previous_balance", precision = 20, scale = 8)
    private BigDecimal previousBalance;

    @Column(name = "available_balance", precision = 20, scale = 8)
    private BigDecimal availableBalance;

    @Column(name = "running_balance", precision = 20, scale = 8)
    private BigDecimal runningBalance;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency;

    @Column(name = "currency_symbol", length = 10)
    private String currencySymbol;

    @Column(name = "exchange_rate", precision = 20, scale = 8)
    @Builder.Default
    private BigDecimal exchangeRate = BigDecimal.ONE;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", nullable = false, length = 20)
    private TransactionStatus currentStatus;


    @Column(name = "status_timeline", columnDefinition = "text")
    @Builder.Default
    private StatusTimeline statusTimeline = new StatusTimeline();

    @Column(length = 500)
    private String description;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "device_id", length = 100)
    private String deviceId;

    @Column(name = "user_agent", length = 500)
    private String userAgent;


    @Column(name = "publish_access", nullable = false)
    @Builder.Default
    private boolean publishAccess = true;

    @Column(name = "admin_note", length = 1000)
    private String adminNote;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    public void advanceStatus(TransactionStatus newStatus, String actor, String message) {
        this.statusTimeline.add(newStatus, actor, message);
        this.currentStatus = newStatus;
        if (newStatus == TransactionStatus.DELIVERED ||
            newStatus == TransactionStatus.SETTLED ||
            newStatus == TransactionStatus.FAILED ||
            newStatus == TransactionStatus.CANCELLED ||
            newStatus == TransactionStatus.REVERSED) {
            this.completedAt = LocalDateTime.now();
        }
    }
}
