package com.epay.domain.bank.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_bank_lists", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_bank_lists_account_number", columnNames = "account_number")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBankList {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ubl_seq")
    @SequenceGenerator(name = "ubl_seq", sequenceName = "user_bank_list_seq", allocationSize = 1)
    private Long id;

    @Column(name = "bank_code")
    private String bankCode;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "account_holder_name")
    private String accountHolderName;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @CreationTimestamp
    @Column(name = "created_on", updatable = false)
    private LocalDateTime createdOn;

    @UpdateTimestamp
    @Column(name = "updated_on")
    private LocalDateTime updatedOn;
}