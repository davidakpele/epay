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
@Table(name = "txn_state_entries", indexes = {
        @Index(name = "idx_tse_transaction_id", columnList = "transaction_id"),
        @Index(name = "idx_tse_state_name",     columnList = "state_name")
})
public class TransactionStateEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tse_seq")
    @SequenceGenerator(name = "tse_seq", sequenceName = "tse_sequence", allocationSize = 50)
    private Long id;

    @Column(name = "transaction_id", nullable = false, length = 50)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "state_name", nullable = false, length = 30)
    private TransactionStatus stateName;

    @Column(name = "visited_at")
    private Instant visitedAt;

    @Column(name = "actor", length = 100)
    private String actor;

    @Column(name = "message", length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "next_state", length = 30)
    private TransactionStatus nextState;

    @Column(name = "on_failure", nullable = false)
    @Builder.Default
    private boolean onFailure = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "failure_state", length = 30)
    private TransactionStatus failureState;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private int sortOrder = 0;
}
