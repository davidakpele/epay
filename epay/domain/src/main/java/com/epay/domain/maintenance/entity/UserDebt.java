package com.epay.domain.maintenance.entity;

import com.epay.domain.maintenance.enums.DebtStatus;
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
    name = "user_debts",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_ud_user_currency",
        columnNames = {"user_id", "currency_code"}
    ),
    indexes = {
        @Index(name = "idx_ud_user_id",   columnList = "user_id"),
        @Index(name = "idx_ud_status",    columnList = "status"),
        @Index(name = "idx_ud_currency",  columnList = "currency_code")
    }
)
public class UserDebt {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ud_seq")
    @SequenceGenerator(name = "ud_seq", sequenceName = "user_debt_seq", allocationSize = 1)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Column(name = "total_debt", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal totalDebt = BigDecimal.ZERO;


    @Column(name = "total_repaid", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal totalRepaid = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private DebtStatus status = DebtStatus.ACTIVE;

    @Column(name = "last_activity_date")
    private LocalDateTime lastActivityDate;

    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version;
}
