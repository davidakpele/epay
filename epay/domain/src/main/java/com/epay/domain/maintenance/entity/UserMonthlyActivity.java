package com.epay.domain.maintenance.entity;

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
    name = "user_monthly_activity",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_uma_user_currency_month",
        columnNames = {"user_id", "currency_code", "month_year"}
    ),
    indexes = {
        @Index(name = "idx_uma_month_processed", columnList = "month_year, processed"),
        @Index(name = "idx_uma_user_month",       columnList = "user_id, month_year")
    }
)
public class UserMonthlyActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "uma_seq")
    @SequenceGenerator(name = "uma_seq", sequenceName = "user_monthly_activity_seq", allocationSize = 1)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Column(name = "month_year", nullable = false)
    private LocalDate monthYear;

    @Column(name = "transaction_count", nullable = false)
    @Builder.Default
    private Integer transactionCount = 0;

    @Column(name = "total_volume", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal totalVolume = BigDecimal.ZERO;

    @Column(name = "has_activity", nullable = false)
    @Builder.Default
    private boolean hasActivity = false;

    @Column(name = "processed", nullable = false)
    @Builder.Default
    private boolean processed = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
