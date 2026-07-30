package com.epay.domain.investment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.epay.domain.investment.enums.InvestmentDuration;
import com.epay.domain.investment.enums.InvestmentStatus;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "investments")
public class Investment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "wallet_id", nullable = false)
    private Long walletId;

    @Column(name = "currency_code", nullable = false, length = 10)
    @Builder.Default
    private String currencyCode = "NGN";

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal principal;

    @Column(name = "return_rate", precision = 5, scale = 2)
    private BigDecimal returnRate;

    @Column(name = "expected_profit", precision = 18, scale = 4)
    private BigDecimal expectedProfit;

    @Column(name = "total_payout", precision = 18, scale = 4)
    private BigDecimal totalPayout;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvestmentDuration duration;

    @Column(name = "duration_days")
    private Integer durationDays;

    @Column(name = "start_date", nullable = false)
    @Builder.Default
    private LocalDateTime startDate = LocalDateTime.now();

    @Column(name = "maturity_date", nullable = false)
    private LocalDateTime maturityDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private InvestmentStatus status = InvestmentStatus.ACTIVE;

    @Column(name = "paid_out_at")
    private LocalDateTime paidOutAt;

    @Column(name = "reference_id", length = 50)
    private String referenceId;

    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn;

    @Column(name = "updated_on", nullable = false)
    private LocalDateTime updatedOn;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdOn = now;
        this.updatedOn = now;

        if (this.startDate == null) {
            this.startDate = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedOn = LocalDateTime.now();
    }
}