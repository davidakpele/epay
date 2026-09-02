package com.epay.domain.admin.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminTransactionDTO {
    private Long   id;
    private String transactionId;
    private String reference;
    private Long   userId;
    private String accountHolder;
    private String transactionType;
    private String debitCredit;
    private String channel;
    private String status;
    private BigDecimal grossAmount;
    private BigDecimal feeAmount;
    private BigDecimal netAmount;
    private String currency;
    private String currencySymbol;
    private BigDecimal previousBalance;
    private BigDecimal runningBalance;
    private String description;
    private String failureReason;
    private String adminNote;
    private boolean amlFlag;
    private String complianceNote;
    private String disputeStatus;
    private String disputeReference;
    private String ipAddress;
    private String reviewedBy;
    private Long   counterpartyUserId;
    private String counterpartyAccountHolder;
    private String bankCode;
    private String bankAccountNumber;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
