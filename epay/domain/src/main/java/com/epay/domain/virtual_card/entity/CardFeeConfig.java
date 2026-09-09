package com.epay.domain.virtual_card.entity;

import com.epay.domain.virtual_card.enums.CardType;
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
    name = "card_fee_config",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_card_fee_currency_type",
        columnNames = {"currency_code", "card_type"}
    ),
    indexes = {
        @Index(name = "idx_card_fee_currency", columnList = "currency_code"),
        @Index(name = "idx_card_fee_active",   columnList = "active")
    }
)
public class CardFeeConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cfc_seq")
    @SequenceGenerator(name = "cfc_seq", sequenceName = "card_fee_config_seq", allocationSize = 1)
    private Long id;
    @Column(name = "currency_code", nullable = false, length = 10)
    private String currencyCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_type", nullable = false, length = 10)
    private CardType cardType;

    @Column(name = "fee_amount", nullable = false, precision = 20, scale = 4)
    private BigDecimal feeAmount;

    @Column(name = "description", length = 255)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "updated_by")
    private Long updatedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version;
}
