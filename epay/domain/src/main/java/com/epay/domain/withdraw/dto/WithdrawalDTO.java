package com.epay.domain.withdraw.dto;

import com.epay.domain.withdraw.enums.WithdrawalStatus;
import com.epay.domain.withdraw.enums.WithdrawalType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WithdrawalDTO {

    private String transactionId;
    private String reference;
    private BigDecimal amount;
    private BigDecimal fee;
    private BigDecimal netAmount;
    private String currency;
    private String currencySymbol;
    private WithdrawalType withdrawalType;
    private WithdrawalStatus status;
    private String bankCode;
    private String accountNumber;
    private String accountName;
    private String narration;
    private BigDecimal previousBalance;
    private BigDecimal newBalance;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String failureReason;

    // ── Paystack checkout fields (populated for bank withdrawals pending payment) ──
    /** Paystack-hosted payment page URL. Redirect the user here to complete payment. */
    private String authorizationUrl;
    /** Paystack access code for the initialized transaction. */
    private String accessCode;
    /** The reference Paystack assigned to this transaction (may differ from internal reference). */
    private String paystackReference;
}
