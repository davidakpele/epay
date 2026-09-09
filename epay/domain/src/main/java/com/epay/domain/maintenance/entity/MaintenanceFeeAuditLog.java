package com.epay.domain.maintenance.entity;

import com.epay.domain.maintenance.enums.MaintenanceAuditAction;
import com.epay.domain.maintenance.enums.MaintenanceFeeStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "maintenance_fee_audit_log",
    indexes = {
        @Index(name = "idx_mfal_batch_id",  columnList = "batch_id"),
        @Index(name = "idx_mfal_user_id",   columnList = "user_id"),
        @Index(name = "idx_mfal_action",    columnList = "action")
    }
)
public class MaintenanceFeeAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mfal_seq")
    @SequenceGenerator(name = "mfal_seq", sequenceName = "maintenance_fee_audit_log_seq", allocationSize = 1)
    private Long id;

    /**
     * UUID of the scheduler batch that produced this entry.
     */
    @Column(name = "batch_id", nullable = false, length = 36)
    private String batchId;

    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 50)
    private MaintenanceAuditAction action;

    /**
     * Free-form JSON details (amounts, currency, balances, etc.).
     * Stored as TEXT for broad PostgreSQL compatibility.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "details", columnDefinition = "text")
    private String details;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private MaintenanceFeeStatus status;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;
}
