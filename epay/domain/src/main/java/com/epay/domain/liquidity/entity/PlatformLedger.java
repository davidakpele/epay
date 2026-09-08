package com.epay.domain.liquidity.entity;

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
@Table(name = "platform_ledger", indexes = {
        @Index(name = "idx_pl_created_at",   columnList = "created_at"),
        @Index(name = "idx_pl_entry_type",   columnList = "entry_type"),
        @Index(name = "idx_pl_gateway",      columnList = "gateway"),
        @Index(name = "idx_pl_reference",    columnList = "reference", unique = true)
})
public class PlatformLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "platform_ledger_seq")
    @SequenceGenerator(name = "platform_ledger_seq", sequenceName = "platform_ledger_sequence", allocationSize = 1)
    private Long id;

    @Column(nullable = false, length = 30)
    private String gateway;

    @Column(name = "entry_type", nullable = false, length = 30)
    private String entryType;

    @Column(nullable = false, precision = 20, scale = 2)
    private BigDecimal amount;

    @Column(name = "balance_after", precision = 20, scale = 2)
    private BigDecimal balanceAfter;

    @Column(nullable = false, length = 10)
    private String currency;

    @Column(unique = true, length = 100)
    private String reference;

    @Column(name = "related_user_id")
    private Long relatedUserId;

    @Column(length = 500)
    private String description;

    @Column(name = "recorded_by", length = 100)
    private String recordedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;
}
