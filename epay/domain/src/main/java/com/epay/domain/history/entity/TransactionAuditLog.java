package com.epay.domain.history.entity;

import com.epay.domain.history.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Append-only audit log — records WHO changed WHAT and WHEN.
 * Never updated or deleted — immutable for compliance.
 *
 * Separate from Transaction which answers "what is the current state?"
 * This answers "who changed it, when, and from where?"
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transaction_audit_log", indexes = {
        @Index(name = "idx_audit_transaction_id", columnList = "transaction_id"),
        @Index(name = "idx_audit_performed_by",   columnList = "performed_by"),
        @Index(name = "idx_audit_created_at",     columnList = "created_at")
})
public class TransactionAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** References Transaction.transactionId (not the DB id). */
    @Column(name = "transaction_id", nullable = false, length = 50)
    private String transactionId;

    /**
     * Action type — e.g. CREATE, STATUS_CHANGE, MANUAL_ADJUSTMENT,
     * DISPUTE_RAISED, REVERSAL_REQUESTED
     */
    @Column(name = "action", nullable = false, length = 50)
    private String action;

    /**
     * Who triggered this action — userId, "SYSTEM", "PAYSTACK", "ADMIN:{id}", etc.
     */
    @Column(name = "performed_by", nullable = false, length = 100)
    private String performedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 20)
    private TransactionStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", length = 20)
    private TransactionStatus newStatus;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "device_id", length = 100)
    private String deviceId;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "metadata", length = 2000)
    private String metadata;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;
}
