package com.epay.domain.history.entity;

import com.epay.domain.history.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "txn_status_history", indexes = {
        @Index(name = "idx_tsh_transaction_id", columnList = "transaction_id"),
        @Index(name = "idx_tsh_status",         columnList = "status"),
        @Index(name = "idx_tsh_visited_at",     columnList = "visited_at")
})
public class TransactionStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tsh_seq")
    @SequenceGenerator(name = "tsh_seq", sequenceName = "tsh_sequence", allocationSize = 50)
    private Long id;

    @Column(name = "transaction_id", nullable = false, length = 50)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private TransactionStatus status;

    @Column(name = "visited_at", nullable = false)
    private Instant visitedAt;

    @Column(name = "actor", length = 100)
    private String actor;

    @Column(name = "message", length = 500)
    private String message;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private int sortOrder = 0;
}
