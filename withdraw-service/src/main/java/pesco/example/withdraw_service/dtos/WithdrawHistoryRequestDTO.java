package pesco.example.withdraw_service.dtos;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;
import pesco.example.withdraw_service.enums.TransactionType;


@Data
@Builder
public class WithdrawHistoryRequestDTO {
        // ── Core Identity ────────────────────────────────────────────
    private Long userId;
    private Long walletId;
    private String transactionId;
    private String accountHolder;          // was fullname
    private String sessionId;
    private String referenceId;
    private String terminalId;
    private String erId;

    // ── Transaction Info ─────────────────────────────────────────
    private TransactionType type;
    private String currencyType;           // send as String not enum
    private String description;
    private String message;
    private String status;
    private String timestamp;
    private String processedAt;
    private String approvalTimestamp;
    private String ipAddress;             // was @JsonProperty("ip_address")

    // ── Financial Amounts ────────────────────────────────────────
    private BigDecimal grossAmount;        // was amount
    private BigDecimal feeAmount;
    private BigDecimal taxAmount;
    private BigDecimal netAmount;
    private BigDecimal previousBalance;
    private BigDecimal availableBalance;   // was newBalance
    private BigDecimal runningBalance;     // new — same value as availableBalance

    // ── rest stays the same ──────────────────────────────────────
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
    private String channel;
    private String deviceId;
    private String userAgent;
    private String geoLocation;
    private BigDecimal riskScore;
    private Boolean amlFlag;
    private String sanctionScreeningResult;
    private String complianceNote;
    private String reviewedBy;
    private String initiatedBy;
    private String approvedBy;
    private String adminNote;
    private Boolean manualAdjustmentFlag;
    private String category;
    private String tags;

    public WithdrawHistoryRequestDTO(){}


    public WithdrawHistoryRequestDTO(Long userId, Long walletId, String transactionId, String accountHolder, String sessionId, String referenceId, String terminalId, String erId, TransactionType type, String currencyType, String description, String message, String status, String timestamp, String processedAt, String approvalTimestamp, String ipAddress, BigDecimal grossAmount, BigDecimal feeAmount, BigDecimal taxAmount, BigDecimal netAmount, BigDecimal previousBalance, BigDecimal availableBalance, BigDecimal runningBalance, String debitCredit, String ledgerEntryType, Long counterpartyWalletId, Long counterpartyUserId, String counterpartyAccountHolder, String bankCode, String bankAccountNumber, String routingNumber, String externalReference, String originalCurrency, BigDecimal exchangeRate, String parentHistoryId, String reversalReason, String disputeStatus, String disputeReference, String idempotencyKey, Integer retryCount, String failureReason, String channel, String deviceId, String userAgent, String geoLocation, BigDecimal riskScore, Boolean amlFlag, String sanctionScreeningResult, String complianceNote, String reviewedBy, String initiatedBy, String approvedBy, String adminNote, Boolean manualAdjustmentFlag, String category, String tags) {
        this.userId = userId;
        this.walletId = walletId;
        this.transactionId = transactionId;
        this.accountHolder = accountHolder;
        this.sessionId = sessionId;
        this.referenceId = referenceId;
        this.terminalId = terminalId;
        this.erId = erId;
        this.type = type;
        this.currencyType = currencyType;
        this.description = description;
        this.message = message;
        this.status = status;
        this.timestamp = timestamp;
        this.processedAt = processedAt;
        this.approvalTimestamp = approvalTimestamp;
        this.ipAddress = ipAddress;
        this.grossAmount = grossAmount;
        this.feeAmount = feeAmount;
        this.taxAmount = taxAmount;
        this.netAmount = netAmount;
        this.previousBalance = previousBalance;
        this.availableBalance = availableBalance;
        this.runningBalance = runningBalance;
        this.debitCredit = debitCredit;
        this.ledgerEntryType = ledgerEntryType;
        this.counterpartyWalletId = counterpartyWalletId;
        this.counterpartyUserId = counterpartyUserId;
        this.counterpartyAccountHolder = counterpartyAccountHolder;
        this.bankCode = bankCode;
        this.bankAccountNumber = bankAccountNumber;
        this.routingNumber = routingNumber;
        this.externalReference = externalReference;
        this.originalCurrency = originalCurrency;
        this.exchangeRate = exchangeRate;
        this.parentHistoryId = parentHistoryId;
        this.reversalReason = reversalReason;
        this.disputeStatus = disputeStatus;
        this.disputeReference = disputeReference;
        this.idempotencyKey = idempotencyKey;
        this.retryCount = retryCount;
        this.failureReason = failureReason;
        this.channel = channel;
        this.deviceId = deviceId;
        this.userAgent = userAgent;
        this.geoLocation = geoLocation;
        this.riskScore = riskScore;
        this.amlFlag = amlFlag;
        this.sanctionScreeningResult = sanctionScreeningResult;
        this.complianceNote = complianceNote;
        this.reviewedBy = reviewedBy;
        this.initiatedBy = initiatedBy;
        this.approvedBy = approvedBy;
        this.adminNote = adminNote;
        this.manualAdjustmentFlag = manualAdjustmentFlag;
        this.category = category;
        this.tags = tags;
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getWalletId() {
        return this.walletId;
    }

    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }

    public String getTransactionId() {
        return this.transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getAccountHolder() {
        return this.accountHolder;
    }

    public void setAccountHolder(String accountHolder) {
        this.accountHolder = accountHolder;
    }

    public String getSessionId() {
        return this.sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getReferenceId() {
        return this.referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getTerminalId() {
        return this.terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public String getErId() {
        return this.erId;
    }

    public void setErId(String erId) {
        this.erId = erId;
    }

    public TransactionType getType() {
        return this.type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public String getCurrencyType() {
        return this.currencyType;
    }

    public void setCurrencyType(String currencyType) {
        this.currencyType = currencyType;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getProcessedAt() {
        return this.processedAt;
    }

    public void setProcessedAt(String processedAt) {
        this.processedAt = processedAt;
    }

    public String getApprovalTimestamp() {
        return this.approvalTimestamp;
    }

    public void setApprovalTimestamp(String approvalTimestamp) {
        this.approvalTimestamp = approvalTimestamp;
    }

    public String getIpAddress() {
        return this.ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public BigDecimal getGrossAmount() {
        return this.grossAmount;
    }

    public void setGrossAmount(BigDecimal grossAmount) {
        this.grossAmount = grossAmount;
    }

    public BigDecimal getFeeAmount() {
        return this.feeAmount;
    }

    public void setFeeAmount(BigDecimal feeAmount) {
        this.feeAmount = feeAmount;
    }

    public BigDecimal getTaxAmount() {
        return this.taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getNetAmount() {
        return this.netAmount;
    }

    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }

    public BigDecimal getPreviousBalance() {
        return this.previousBalance;
    }

    public void setPreviousBalance(BigDecimal previousBalance) {
        this.previousBalance = previousBalance;
    }

    public BigDecimal getAvailableBalance() {
        return this.availableBalance;
    }

    public void setAvailableBalance(BigDecimal availableBalance) {
        this.availableBalance = availableBalance;
    }

    public BigDecimal getRunningBalance() {
        return this.runningBalance;
    }

    public void setRunningBalance(BigDecimal runningBalance) {
        this.runningBalance = runningBalance;
    }

    public String getDebitCredit() {
        return this.debitCredit;
    }

    public void setDebitCredit(String debitCredit) {
        this.debitCredit = debitCredit;
    }

    public String getLedgerEntryType() {
        return this.ledgerEntryType;
    }

    public void setLedgerEntryType(String ledgerEntryType) {
        this.ledgerEntryType = ledgerEntryType;
    }

    public Long getCounterpartyWalletId() {
        return this.counterpartyWalletId;
    }

    public void setCounterpartyWalletId(Long counterpartyWalletId) {
        this.counterpartyWalletId = counterpartyWalletId;
    }

    public Long getCounterpartyUserId() {
        return this.counterpartyUserId;
    }

    public void setCounterpartyUserId(Long counterpartyUserId) {
        this.counterpartyUserId = counterpartyUserId;
    }

    public String getCounterpartyAccountHolder() {
        return this.counterpartyAccountHolder;
    }

    public void setCounterpartyAccountHolder(String counterpartyAccountHolder) {
        this.counterpartyAccountHolder = counterpartyAccountHolder;
    }

    public String getBankCode() {
        return this.bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getBankAccountNumber() {
        return this.bankAccountNumber;
    }

    public void setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }

    public String getRoutingNumber() {
        return this.routingNumber;
    }

    public void setRoutingNumber(String routingNumber) {
        this.routingNumber = routingNumber;
    }

    public String getExternalReference() {
        return this.externalReference;
    }

    public void setExternalReference(String externalReference) {
        this.externalReference = externalReference;
    }

    public String getOriginalCurrency() {
        return this.originalCurrency;
    }

    public void setOriginalCurrency(String originalCurrency) {
        this.originalCurrency = originalCurrency;
    }

    public BigDecimal getExchangeRate() {
        return this.exchangeRate;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public String getParentHistoryId() {
        return this.parentHistoryId;
    }

    public void setParentHistoryId(String parentHistoryId) {
        this.parentHistoryId = parentHistoryId;
    }

    public String getReversalReason() {
        return this.reversalReason;
    }

    public void setReversalReason(String reversalReason) {
        this.reversalReason = reversalReason;
    }

    public String getDisputeStatus() {
        return this.disputeStatus;
    }

    public void setDisputeStatus(String disputeStatus) {
        this.disputeStatus = disputeStatus;
    }

    public String getDisputeReference() {
        return this.disputeReference;
    }

    public void setDisputeReference(String disputeReference) {
        this.disputeReference = disputeReference;
    }

    public String getIdempotencyKey() {
        return this.idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public Integer getRetryCount() {
        return this.retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public String getFailureReason() {
        return this.failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public String getChannel() {
        return this.channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getUserAgent() {
        return this.userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getGeoLocation() {
        return this.geoLocation;
    }

    public void setGeoLocation(String geoLocation) {
        this.geoLocation = geoLocation;
    }

    public BigDecimal getRiskScore() {
        return this.riskScore;
    }

    public void setRiskScore(BigDecimal riskScore) {
        this.riskScore = riskScore;
    }

    public Boolean isAmlFlag() {
        return this.amlFlag;
    }

    public Boolean getAmlFlag() {
        return this.amlFlag;
    }

    public void setAmlFlag(Boolean amlFlag) {
        this.amlFlag = amlFlag;
    }

    public String getSanctionScreeningResult() {
        return this.sanctionScreeningResult;
    }

    public void setSanctionScreeningResult(String sanctionScreeningResult) {
        this.sanctionScreeningResult = sanctionScreeningResult;
    }

    public String getComplianceNote() {
        return this.complianceNote;
    }

    public void setComplianceNote(String complianceNote) {
        this.complianceNote = complianceNote;
    }

    public String getReviewedBy() {
        return this.reviewedBy;
    }

    public void setReviewedBy(String reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public String getInitiatedBy() {
        return this.initiatedBy;
    }

    public void setInitiatedBy(String initiatedBy) {
        this.initiatedBy = initiatedBy;
    }

    public String getApprovedBy() {
        return this.approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public String getAdminNote() {
        return this.adminNote;
    }

    public void setAdminNote(String adminNote) {
        this.adminNote = adminNote;
    }

    public Boolean isManualAdjustmentFlag() {
        return this.manualAdjustmentFlag;
    }

    public Boolean getManualAdjustmentFlag() {
        return this.manualAdjustmentFlag;
    }

    public void setManualAdjustmentFlag(Boolean manualAdjustmentFlag) {
        this.manualAdjustmentFlag = manualAdjustmentFlag;
    }

    public String getCategory() {
        return this.category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTags() {
        return this.tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

}
