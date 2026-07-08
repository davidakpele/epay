package com.epay.history.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transaction_history", indexes = {
        @Index(name = "idx_th_user_id",       columnList = "user_id"),
        @Index(name = "idx_th_wallet_id",      columnList = "wallet_id"),
        @Index(name = "idx_th_reference",      columnList = "reference"),
        @Index(name = "idx_th_transaction_id", columnList = "transaction_id"),
        @Index(name = "idx_th_type",           columnList = "transaction_type"),
        @Index(name = "idx_th_status",         columnList = "status"),
        @Index(name = "idx_th_created_at",     columnList = "created_at"),
        @Index(name = "idx_th_idempotency",    columnList = "idempotency_key", unique = true)
})
public class TransactionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "history_seq")
    @SequenceGenerator(name = "history_seq", sequenceName = "history_sequence", allocationSize = 1)
    private Long id;

    @Column(name = "transaction_id", nullable = false)
    private String transactionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "wallet_id")
    private Long walletId;

    @Column(name = "account_holder")
    private String accountHolder;

    @Column(name = "reference")
    private String reference;

    @Column(name = "gateway_reference")
    private String gatewayReference;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "idempotency_key", unique = true)
    private String idempotencyKey;

    @Column(name = "transaction_type", nullable = false)
    private String transactionType;

    @Column(name = "debit_credit", nullable = false)
    private String debitCredit;

    @Column(name = "channel")
    private String channel;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(length = 500)
    private String description;

    @Column(length = 1000)
    private String message;

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

    @Column(name = "currency_type", nullable = false, length = 10)
    private String currencyType;

    @Column(name = "currency_symbol", length = 10)
    private String currencySymbol;

    @Column(name = "original_currency", length = 10)
    private String originalCurrency;

    @Column(name = "exchange_rate", precision = 20, scale = 8)
    @Builder.Default
    private BigDecimal exchangeRate = BigDecimal.ONE;

    @Column(name = "counterparty_wallet_id")
    private Long counterpartyWalletId;

    @Column(name = "counterparty_user_id")
    private Long counterpartyUserId;

    @Column(name = "counterparty_account_holder")
    private String counterpartyAccountHolder;

    @Column(name = "bank_code")
    private String bankCode;

    @Column(name = "bank_account_number")
    private String bankAccountNumber;

    @Column(name = "routing_number")
    private String routingNumber;

    @Column(name = "external_reference")
    private String externalReference;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "geo_location")
    private String geoLocation;

    @Column(name = "risk_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal riskScore = BigDecimal.ZERO;

    @Column(name = "aml_flag")
    @Builder.Default
    private boolean amlFlag = false;

    @Column(name = "sanction_screening_result")
    private String sanctionScreeningResult;

    @Column(name = "compliance_note", length = 1000)
    private String complianceNote;

    @Column(name = "reviewed_by")
    private String reviewedBy;

    @Column(name = "parent_history_id")
    private Long parentHistoryId;

    @Column(name = "reversal_reason")
    private String reversalReason;

    @Column(name = "dispute_status")
    private String disputeStatus;

    @Column(name = "dispute_reference")
    private String disputeReference;

    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "processed_at")
    private String processedAt;

    @Column(name = "initiated_by")
    private String initiatedBy;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approval_timestamp")
    private String approvalTimestamp;

    @Column(name = "admin_note", length = 1000)
    private String adminNote;

    @Column(name = "manual_adjustment_flag")
    @Builder.Default
    private boolean manualAdjustmentFlag = false;

    @Column(name = "category")
    private String category;

    @Column(name = "tags")
    private String tags;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;
}
