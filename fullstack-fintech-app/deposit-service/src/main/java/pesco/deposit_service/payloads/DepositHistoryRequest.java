package pesco.deposit_service.payloads;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonProperty;
import pesco.deposit_service.enums.CurrencyType;
import pesco.deposit_service.enums.TransactionType;

public class DepositHistoryRequest {

    // ── Core Identity ────────────────────────────────────────────
    private Long userId;
    private Long walletId;
    private String transactionId;
    private String fullname;                // → AccountHolder
    private String sessionId;              // → SessionId
    private String referenceId;            // → ReferenceId (referenceNo)
    private String terminalId;             // → TerminalId
    private String erId;                   // → ErId

    // ── Transaction Info ─────────────────────────────────────────
    private TransactionType type;
    private CurrencyType currencyType;
    private String description;
    private String message;
    private String status;
    private String timestamp;           // was Long → now ISO-8601 String
    private String processedAt;         // was Long → now ISO-8601 String
    private String approvalTimestamp;   // was Long → now ISO-8601 String               // → Timestamp (epoch ms → DateTime on C#)

    @JsonProperty("ip_address")
    private String ipAddress;

    // ── Financial Amounts ────────────────────────────────────────
    private BigDecimal amount;             // → GrossAmount
    private BigDecimal feeAmount;          // → FeeAmount
    private BigDecimal taxAmount;          // → TaxAmount
    private BigDecimal netAmount;          // → NetAmount
    private BigDecimal previousBalance;    // → PreviousBalance
    private BigDecimal newBalance;         // → AvailableBalance + RunningBalance

    // ── Double-Entry Accounting ──────────────────────────────────
    private String debitCredit;            // → DebitCredit
    private String ledgerEntryType;        // → LedgerEntryType

    // ── Counterparty & Routing ───────────────────────────────────
    private Long counterpartyWalletId;     // → CounterpartyWalletId
    private Long counterpartyUserId;       // → CounterpartyUserId
    private String counterpartyAccountHolder; // → CounterpartyAccountHolder
    private String bankCode;               // → BankCode
    private String bankAccountNumber;      // → BankAccountNumber
    private String routingNumber;          // → RoutingNumber
    private String externalReference;      // → ExternalReference

    // ── Multi-Currency ───────────────────────────────────────────
    private String originalCurrency;       // → OriginalCurrency
    private BigDecimal exchangeRate;       // → ExchangeRate

    // ── Reversal & Disputes ──────────────────────────────────────
    private String parentHistoryId;        // → ParentHistoryId
    private String reversalReason;         // → ReversalReason
    private String disputeStatus;          // → DisputeStatus
    private String disputeReference;       // → DisputeReference

    // ── Idempotency & Retry ──────────────────────────────────────
    private String idempotencyKey;         // → IdempotencyKey
    private Integer retryCount;            // → RetryCount
    private String failureReason;  

    // ── Channel & Device ─────────────────────────────────────────
    private String channel;                // → TransactionChannel
    private String deviceId;              // → DeviceId
    private String userAgent;             // → UserAgent
    private String geoLocation;           // → GeoLocation

    // ── Compliance & Risk ────────────────────────────────────────
    private BigDecimal riskScore;          // → RiskScore
    private Boolean amlFlag;              // → AmlFlag
    private String sanctionScreeningResult; // → SanctionScreeningResult
    private String complianceNote;         // → ComplianceNote
    private String reviewedBy;             // → ReviewedBy

    // ── Admin Audit ──────────────────────────────────────────────
    private String initiatedBy;            // → InitiatedBy
    private String approvedBy;             // → ApprovedBy
    private String adminNote;             // → AdminNote
    private Boolean manualAdjustmentFlag; // → ManualAdjustmentFlag

    // ── Metadata ─────────────────────────────────────────────────
    private String category;              // → TransactionCategory
    private String tags;                  // → Tags (JSON string)


    public DepositHistoryRequest() {
    }

    public DepositHistoryRequest(Long userId, Long walletId, String transactionId, String fullname, String sessionId, String referenceId, String terminalId, String erId, TransactionType type, CurrencyType currencyType, String description, String message, String status, String timestamp, String processedAt, String approvalTimestamp, String ipAddress, BigDecimal amount, BigDecimal feeAmount, BigDecimal taxAmount, BigDecimal netAmount, BigDecimal previousBalance, BigDecimal newBalance, String debitCredit, String ledgerEntryType, Long counterpartyWalletId, Long counterpartyUserId, String counterpartyAccountHolder, String bankCode, String bankAccountNumber, String routingNumber, String externalReference, String originalCurrency, BigDecimal exchangeRate, String parentHistoryId, String reversalReason, String disputeStatus, String disputeReference, String idempotencyKey, Integer retryCount, String failureReason, String channel, String deviceId, String userAgent, String geoLocation, BigDecimal riskScore, Boolean amlFlag, String sanctionScreeningResult, String complianceNote, String reviewedBy, String initiatedBy, String approvedBy, String adminNote, Boolean manualAdjustmentFlag, String category, String tags) {
        this.userId = userId;
        this.walletId = walletId;
        this.transactionId = transactionId;
        this.fullname = fullname;
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
        this.amount = amount;
        this.feeAmount = feeAmount;
        this.taxAmount = taxAmount;
        this.netAmount = netAmount;
        this.previousBalance = previousBalance;
        this.newBalance = newBalance;
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

    public String getFullname() {
        return this.fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
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

    public CurrencyType getCurrencyType() {
        return this.currencyType;
    }

    public void setCurrencyType(CurrencyType currencyType) {
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

    public BigDecimal getAmount() {
        return this.amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
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

    public BigDecimal getNewBalance() {
        return this.newBalance;
    }

    public void setNewBalance(BigDecimal newBalance) {
        this.newBalance = newBalance;
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