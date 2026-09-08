package com.epay.domain.history.entity;

import com.epay.domain.history.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "txn_allowed_transitions", indexes = {
        @Index(name = "idx_tat_transaction_id", columnList = "transaction_id"),
        @Index(name = "idx_tat_from_state",     columnList = "from_state"),
        @Index(name = "idx_tat_to_state",       columnList = "to_state")
})
public class TransactionAllowedTransition {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tat_seq")
    @SequenceGenerator(name = "tat_seq", sequenceName = "tat_sequence", allocationSize = 50)
    private Long id;

    @Column(name = "transaction_id", nullable = false, length = 50)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_state", nullable = false, length = 30)
    private TransactionStatus fromState;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_state", nullable = false, length = 30)
    private TransactionStatus toState;
    
    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private int sortOrder = 0;
}
