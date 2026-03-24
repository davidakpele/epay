package pesco.deposit_service.payloads;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import pesco.deposit_service.enums.CurrencyType;
import pesco.deposit_service.enums.TransactionType;
@Data
public class DepositHistoryRequest {

    // ── Core Identity ────────────────────────────────────────────
    private Long userId;
    private Long walletId;
    private String transactionId;
    private String fullname;            // → AccountHolder

    // ── Transaction Info ─────────────────────────────────────────
    private TransactionType type;
    private CurrencyType currencyType;
    private String description;
    private String message;

    @JsonProperty("ip_address")
    private String ipAddress;

    // ── Financial Amounts ────────────────────────────────────────
    private BigDecimal amount;          // → GrossAmount
    private BigDecimal feeAmount;       // → FeeAmount
    private BigDecimal taxAmount;       // → TaxAmount
    private BigDecimal netAmount;       // → NetAmount
    private BigDecimal previousBalance; // → PreviousBalance
    private BigDecimal newBalance;      // → AvailableBalance + RunningBalance

    // ── Double-Entry Accounting ──────────────────────────────────
    private String debitCredit;         // → DebitCredit
    private String ledgerEntryType;     // → LedgerEntryType

    // ── Channel & Device ─────────────────────────────────────────
    private String channel;             // → TransactionChannel
    private String deviceId;            // → DeviceId
    private String userAgent;           // → UserAgent
    private String geoLocation;         // → GeoLocation

    // ── Idempotency ──────────────────────────────────────────────
    private String idempotencyKey;      // → IdempotencyKey

    // ── Multi-Currency ───────────────────────────────────────────
    private String originalCurrency;    // → OriginalCurrency
    private BigDecimal exchangeRate;    // → ExchangeRate

    // ── Metadata ─────────────────────────────────────────────────
    private String category;            // → TransactionCategory

    private String initiatedBy;     // → InitiatedBy  (set to userId.toString())
    private String status;          // → Status        (set to "SUCCESS")
    private Long processedAt;       // → ProcessedAt   (set to System.currentTimeMillis())

    public DepositHistoryRequest() {
    }

    public DepositHistoryRequest(Long userId, Long walletId, String transactionId, String fullname, TransactionType type, CurrencyType currencyType, String description, String message, String ipAddress, BigDecimal amount, BigDecimal feeAmount, BigDecimal taxAmount, BigDecimal netAmount, BigDecimal previousBalance, BigDecimal newBalance, String debitCredit, String ledgerEntryType, String channel, String deviceId, String userAgent, String geoLocation, String idempotencyKey, String originalCurrency, BigDecimal exchangeRate, String category, String initiatedBy, String status, Long processedAt) {
        this.userId = userId;
        this.walletId = walletId;
        this.transactionId = transactionId;
        this.fullname = fullname;
        this.type = type;
        this.currencyType = currencyType;
        this.description = description;
        this.message = message;
        this.ipAddress = ipAddress;
        this.amount = amount;
        this.feeAmount = feeAmount;
        this.taxAmount = taxAmount;
        this.netAmount = netAmount;
        this.previousBalance = previousBalance;
        this.newBalance = newBalance;
        this.debitCredit = debitCredit;
        this.ledgerEntryType = ledgerEntryType;
        this.channel = channel;
        this.deviceId = deviceId;
        this.userAgent = userAgent;
        this.geoLocation = geoLocation;
        this.idempotencyKey = idempotencyKey;
        this.originalCurrency = originalCurrency;
        this.exchangeRate = exchangeRate;
        this.category = category;
        this.initiatedBy = initiatedBy;
        this.status = status;
        this.processedAt = processedAt;
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

    public String getIdempotencyKey() {
        return this.idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
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

    public String getCategory() {
        return this.category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getInitiatedBy() {
        return this.initiatedBy;
    }

    public void setInitiatedBy(String initiatedBy) {
        this.initiatedBy = initiatedBy;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getProcessedAt() {
        return this.processedAt;
    }

    public void setProcessedAt(Long processedAt) {
        this.processedAt = processedAt;
    }


}