package com.epay.domain.beneficiary.entity;

import com.epay.domain.beneficiary.enums.BeneficiaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "beneficiaries",
    indexes = {
        @Index(name = "idx_beneficiary_user_id",   columnList = "user_id"),
        @Index(name = "idx_beneficiary_type",       columnList = "user_id, beneficiary_type"),
        @Index(name = "idx_beneficiary_active",     columnList = "user_id, is_active")
    }
)
public class Beneficiary {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ben_seq")
    @SequenceGenerator(name = "ben_seq", sequenceName = "beneficiary_seq", allocationSize = 1)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "beneficiary_type", nullable = false, length = 10)
    private BeneficiaryType beneficiaryType;

    @Column(name = "beneficiary_name", nullable = false, length = 150)
    private String beneficiaryName;

    @Column(name = "currency", nullable = false, length = 10)
    @Builder.Default
    private String currency = "NGN";

    @Column(name = "account_number", length = 20)
    private String accountNumber;

    @Column(name = "account_name", length = 150)
    private String accountName;

    @Column(name = "bank_code", length = 20)
    private String bankCode;

    @Column(name = "bank_name", length = 150)
    private String bankName;

    @Column(name = "recipient_username", length = 100)
    private String recipientUsername;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}