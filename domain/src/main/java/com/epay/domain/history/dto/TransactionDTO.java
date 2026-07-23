package com.epay.domain.history.dto;

import com.epay.domain.history.enums.TransactionStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * API response DTO for transaction history.
 * Matches the response format defined in the Enterprise History Design doc.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionDTO {

    private String transactionId;
    private String reference;
    private String transactionType;
    private String debitCredit;
    private String channel;

    private UserInfo user;
    private UserInfo recipient;

    private AmountInfo amount;
    private BalanceInfo balance;

    private TransactionStatus currentStatus;
    private StatusTimeline statusTimeline;

    private String description;
    private String failureReason;

    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UserInfo {
        private Long id;
        private Long walletId;
        private String accountHolder;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AmountInfo {
        private BigDecimal gross;
        private BigDecimal fee;
        private BigDecimal tax;
        private BigDecimal net;
        private String currency;
        private String symbol;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BalanceInfo {
        private BigDecimal previous;
        private BigDecimal available;
        private BigDecimal running;
    }
}
