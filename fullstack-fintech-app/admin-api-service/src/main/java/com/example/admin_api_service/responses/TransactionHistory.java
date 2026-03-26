package com.example.admin_api_service.responses;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionHistory {

    private String id;
    private Long walletId;
    private Long userId;
    private String sessionId;
    private String transactionId;
    private String referenceId;
    private String terminalId;
    private String erId;
    private String accountHolder;
    private String type;
    private String description;
    private String message;
    private String currencyType;
    private String status;
    private String ipAddress;
    private Instant timestamp;
    private Instant createdOn;
    private Instant updatedOn;
    private BigDecimal grossAmount;
    private BigDecimal feeAmount;
    private BigDecimal taxAmount;
    private BigDecimal netAmount;
    private BigDecimal previousBalance;
    private BigDecimal availableBalance;
    private BigDecimal runningBalance;
    private String debitCredit;
    private String ledgerEntryType;
    private Long counterpartyWalletId;
    private Long counterpartyUserId;
    private String counterpartyAccountHolder;
    private String bankCode;
    private String bankAccountNumber;
    private String routingNumber;
    private String externalReference;
    private String originalCurrency;
    private BigDecimal exchangeRate;
    private String parentHistoryId;
    private String reversalReason;
    private String disputeStatus;
    private String disputeReference;
    private String idempotencyKey;
    private Integer retryCount;
    private String failureReason;
    private Instant processedAt;
    private String channel;
    private String deviceId;
    private String userAgent;
    private String geoLocation;
    private Integer riskScore;
    private Boolean amlFlag;
    private String sanctionScreeningResult;
    private String complianceNote;
    private String reviewedBy;
    private String initiatedBy;
    private String approvedBy;
    private Instant approvalTimestamp;
    private String adminNote;
    private Boolean manualAdjustmentFlag;
    private String category;
    private String tags;

    
}