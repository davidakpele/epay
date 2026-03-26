package pesco.example.withdraw_service.payloads;

import java.math.BigDecimal;

import pesco.example.withdraw_service.enums.TransactionType;

public class CreditHistoryRequest {

    // ── Core Identity ────────────────────────────────────────────
    private Long   userId;
    private Long   walletId;
    private String transactionId;
    private String accountHolder;           // display name of the owner of this history entry
    private String sessionId;
    private String referenceId;
    private String terminalId;
    private String erId;

    // ── Transaction Info ─────────────────────────────────────────
    private TransactionType type;           // DEBITED or CREDITED
    private String          currencyType;   // send as String, not enum
    private String          description;
    private String          message;
    private String          status;
    private String          timestamp;
    private String          processedAt;
    private String          approvalTimestamp;
    private String          ipAddress;

    // ── Financial Amounts ────────────────────────────────────────
    private BigDecimal grossAmount;         // transfer amount (before fee)
    private BigDecimal feeAmount;
    private BigDecimal taxAmount;
    private BigDecimal netAmount;           // grossAmount − feeAmount − taxAmount
    private BigDecimal previousBalance;
    private BigDecimal availableBalance;    // balance after this transaction
    private BigDecimal runningBalance;      // same as availableBalance

    // ── Double-Entry Accounting ──────────────────────────────────
    private String debitCredit;             // "DEBIT" or "CREDIT"
    private String ledgerEntryType;         // e.g. "TRANSFER_OUT", "TRANSFER_IN"

    // ── Counterparty ─────────────────────────────────────────────
    private Long   counterpartyUserId;
    private Long   counterpartyWalletId;
    private String counterpartyAccountHolder;
    private String bankCode;
    private String bankAccountNumber;
    private String routingNumber;
    private String externalReference;

    // ── Multi-Currency ───────────────────────────────────────────
    private String     originalCurrency;
    private BigDecimal exchangeRate;

    // ── Reversal & Disputes ──────────────────────────────────────
    private String parentHistoryId;
    private String reversalReason;
    private String disputeStatus;
    private String disputeReference;

    // ── Idempotency & Retry ──────────────────────────────────────
    private String  idempotencyKey;
    private Integer retryCount;
    private String  failureReason;

    // ── Channel & Device ─────────────────────────────────────────
    private String channel;
    private String deviceId;
    private String userAgent;
    private String geoLocation;

    // ── Compliance & Risk ────────────────────────────────────────
    private BigDecimal riskScore;
    private Boolean    amlFlag;
    private String     sanctionScreeningResult;
    private String     complianceNote;
    private String     reviewedBy;

    // ── Admin Audit ──────────────────────────────────────────────
    private String  initiatedBy;
    private String  approvedBy;
    private String  adminNote;
    private Boolean manualAdjustmentFlag;

    // ── Metadata ─────────────────────────────────────────────────
    private String category;
    private String tags;

    // ── Note (user-supplied memo) ────────────────────────────────
    private String note;

    // ─────────────────────────────────────────────────────────────
    // Getters & Setters
    // ─────────────────────────────────────────────────────────────

    public Long getUserId()                          { return userId; }
    public void setUserId(Long userId)               { this.userId = userId; }

    public Long getWalletId()                        { return walletId; }
    public void setWalletId(Long walletId)           { this.walletId = walletId; }

    public String getTransactionId()                 { return transactionId; }
    public void setTransactionId(String transactionId){ this.transactionId = transactionId; }

    public String getAccountHolder()                 { return accountHolder; }
    public void setAccountHolder(String accountHolder){ this.accountHolder = accountHolder; }

    public String getSessionId()                     { return sessionId; }
    public void setSessionId(String sessionId)       { this.sessionId = sessionId; }

    public String getReferenceId()                   { return referenceId; }
    public void setReferenceId(String referenceId)   { this.referenceId = referenceId; }

    public String getTerminalId()                    { return terminalId; }
    public void setTerminalId(String terminalId)     { this.terminalId = terminalId; }

    public String getErId()                          { return erId; }
    public void setErId(String erId)                 { this.erId = erId; }

    public TransactionType getType()                 { return type; }
    public void setType(TransactionType type)        { this.type = type; }

    public String getCurrencyType()                  { return currencyType; }
    public void setCurrencyType(String currencyType) { this.currencyType = currencyType; }

    public String getDescription()                   { return description; }
    public void setDescription(String description)   { this.description = description; }

    public String getMessage()                       { return message; }
    public void setMessage(String message)           { this.message = message; }

    public String getStatus()                        { return status; }
    public void setStatus(String status)             { this.status = status; }

    public String getTimestamp()                     { return timestamp; }
    public void setTimestamp(String timestamp)       { this.timestamp = timestamp; }

    public String getProcessedAt()                   { return processedAt; }
    public void setProcessedAt(String processedAt)   { this.processedAt = processedAt; }

    public String getApprovalTimestamp()             { return approvalTimestamp; }
    public void setApprovalTimestamp(String t)       { this.approvalTimestamp = t; }

    public String getIpAddress()                     { return ipAddress; }
    public void setIpAddress(String ipAddress)       { this.ipAddress = ipAddress; }

    public BigDecimal getGrossAmount()               { return grossAmount; }
    public void setGrossAmount(BigDecimal grossAmount){ this.grossAmount = grossAmount; }

    public BigDecimal getFeeAmount()                 { return feeAmount; }
    public void setFeeAmount(BigDecimal feeAmount)   { this.feeAmount = feeAmount; }

    public BigDecimal getTaxAmount()                 { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount)   { this.taxAmount = taxAmount; }

    public BigDecimal getNetAmount()                 { return netAmount; }
    public void setNetAmount(BigDecimal netAmount)   { this.netAmount = netAmount; }

    public BigDecimal getPreviousBalance()           { return previousBalance; }
    public void setPreviousBalance(BigDecimal b)     { this.previousBalance = b; }

    public BigDecimal getAvailableBalance()          { return availableBalance; }
    public void setAvailableBalance(BigDecimal b)    { this.availableBalance = b; }

    public BigDecimal getRunningBalance()            { return runningBalance; }
    public void setRunningBalance(BigDecimal b)      { this.runningBalance = b; }

    public String getDebitCredit()                   { return debitCredit; }
    public void setDebitCredit(String debitCredit)   { this.debitCredit = debitCredit; }

    public String getLedgerEntryType()               { return ledgerEntryType; }
    public void setLedgerEntryType(String t)         { this.ledgerEntryType = t; }

    public Long getCounterpartyUserId()              { return counterpartyUserId; }
    public void setCounterpartyUserId(Long id)       { this.counterpartyUserId = id; }

    public Long getCounterpartyWalletId()            { return counterpartyWalletId; }
    public void setCounterpartyWalletId(Long id)     { this.counterpartyWalletId = id; }

    public String getCounterpartyAccountHolder()     { return counterpartyAccountHolder; }
    public void setCounterpartyAccountHolder(String s){ this.counterpartyAccountHolder = s; }

    public String getBankCode()                      { return bankCode; }
    public void setBankCode(String bankCode)         { this.bankCode = bankCode; }

    public String getBankAccountNumber()             { return bankAccountNumber; }
    public void setBankAccountNumber(String s)       { this.bankAccountNumber = s; }

    public String getRoutingNumber()                 { return routingNumber; }
    public void setRoutingNumber(String routingNumber){ this.routingNumber = routingNumber; }

    public String getExternalReference()             { return externalReference; }
    public void setExternalReference(String s)       { this.externalReference = s; }

    public String getOriginalCurrency()              { return originalCurrency; }
    public void setOriginalCurrency(String s)        { this.originalCurrency = s; }

    public BigDecimal getExchangeRate()              { return exchangeRate; }
    public void setExchangeRate(BigDecimal exchangeRate){ this.exchangeRate = exchangeRate; }

    public String getParentHistoryId()               { return parentHistoryId; }
    public void setParentHistoryId(String id)        { this.parentHistoryId = id; }

    public String getReversalReason()                { return reversalReason; }
    public void setReversalReason(String s)          { this.reversalReason = s; }

    public String getDisputeStatus()                 { return disputeStatus; }
    public void setDisputeStatus(String s)           { this.disputeStatus = s; }

    public String getDisputeReference()              { return disputeReference; }
    public void setDisputeReference(String s)        { this.disputeReference = s; }

    public String getIdempotencyKey()                { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey){ this.idempotencyKey = idempotencyKey; }

    public Integer getRetryCount()                   { return retryCount; }
    public void setRetryCount(Integer retryCount)    { this.retryCount = retryCount; }

    public String getFailureReason()                 { return failureReason; }
    public void setFailureReason(String s)           { this.failureReason = s; }

    public String getChannel()                       { return channel; }
    public void setChannel(String channel)           { this.channel = channel; }

    public String getDeviceId()                      { return deviceId; }
    public void setDeviceId(String deviceId)         { this.deviceId = deviceId; }

    public String getUserAgent()                     { return userAgent; }
    public void setUserAgent(String userAgent)       { this.userAgent = userAgent; }

    public String getGeoLocation()                   { return geoLocation; }
    public void setGeoLocation(String geoLocation)   { this.geoLocation = geoLocation; }

    public BigDecimal getRiskScore()                 { return riskScore; }
    public void setRiskScore(BigDecimal riskScore)   { this.riskScore = riskScore; }

    public Boolean getAmlFlag()                      { return amlFlag; }
    public void setAmlFlag(Boolean amlFlag)          { this.amlFlag = amlFlag; }

    public String getSanctionScreeningResult()       { return sanctionScreeningResult; }
    public void setSanctionScreeningResult(String s) { this.sanctionScreeningResult = s; }

    public String getComplianceNote()                { return complianceNote; }
    public void setComplianceNote(String s)          { this.complianceNote = s; }

    public String getReviewedBy()                    { return reviewedBy; }
    public void setReviewedBy(String reviewedBy)     { this.reviewedBy = reviewedBy; }

    public String getInitiatedBy()                   { return initiatedBy; }
    public void setInitiatedBy(String initiatedBy)   { this.initiatedBy = initiatedBy; }

    public String getApprovedBy()                    { return approvedBy; }
    public void setApprovedBy(String approvedBy)     { this.approvedBy = approvedBy; }

    public String getAdminNote()                     { return adminNote; }
    public void setAdminNote(String adminNote)       { this.adminNote = adminNote; }

    public Boolean getManualAdjustmentFlag()         { return manualAdjustmentFlag; }
    public void setManualAdjustmentFlag(Boolean b)   { this.manualAdjustmentFlag = b; }

    public String getCategory()                      { return category; }
    public void setCategory(String category)         { this.category = category; }

    public String getTags()                          { return tags; }
    public void setTags(String tags)                 { this.tags = tags; }

    public String getNote()                          { return note; }
    public void setNote(String note)                 { this.note = note; }
}
