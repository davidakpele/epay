package pesco.example.withdraw_service.dtos;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WithdrawHistoryRequestDTO {
     private Long walletId;
    private Long userId;
    private String sessionId;
    private String transactionId;
    private String referenceNo;
    private String terminalId;
    private String erId;
    private String accountHolder;
    private BigDecimal previousBalance;
    private BigDecimal availableBalance;
    private BigDecimal amount;
    private String type;
    private String description;
    private String message;
    private String currencyType;
    private String status;
    private String ipAddress;
    private String timestamp;
    private Long recipientUserId;
    private Long recipientWalletId;
    private String receiverFullName;
    private String senderFullName;
    private String fullname;

    public WithdrawHistoryRequestDTO(){}


    public WithdrawHistoryRequestDTO(Long walletId, Long userId, String sessionId, String transactionId, String referenceNo, String terminalId, String erId, String accountHolder, BigDecimal previousBalance, BigDecimal availableBalance, BigDecimal amount, String type, String description, String message, String currencyType, String status, String ipAddress, String timestamp, Long recipientUserId, Long recipientWalletId, String receiverFullName, String senderFullName, String fullname) {
        this.walletId = walletId;
        this.userId = userId;
        this.sessionId = sessionId;
        this.transactionId = transactionId;
        this.referenceNo = referenceNo;
        this.terminalId = terminalId;
        this.erId = erId;
        this.accountHolder = accountHolder;
        this.previousBalance = previousBalance;
        this.availableBalance = availableBalance;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.message = message;
        this.currencyType = currencyType;
        this.status = status;
        this.ipAddress = ipAddress;
        this.timestamp = timestamp;
        this.recipientUserId = recipientUserId;
        this.recipientWalletId = recipientWalletId;
        this.receiverFullName = receiverFullName;
        this.senderFullName = senderFullName;
        this.fullname = fullname;
    }
   

    public Long getWalletId() {
        return this.walletId;
    }

    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getSessionId() {
        return this.sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getTransactionId() {
        return this.transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getReferenceNo() {
        return this.referenceNo;
    }

    public void setReferenceNo(String referenceNo) {
        this.referenceNo = referenceNo;
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

    public String getAccountHolder() {
        return this.accountHolder;
    }

    public void setAccountHolder(String accountHolder) {
        this.accountHolder = accountHolder;
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

    public BigDecimal getAmount() {
        return this.amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getCurrencyType() {
        return this.currencyType;
    }

    public void setCurrencyType(String currencyType) {
        this.currencyType = currencyType;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIpAddress() {
        return this.ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Long getRecipientUserId() {
        return this.recipientUserId;
    }

    public void setRecipientUserId(Long recipientUserId) {
        this.recipientUserId = recipientUserId;
    }

    public Long getRecipientWalletId() {
        return this.recipientWalletId;
    }

    public void setRecipientWalletId(Long recipientWalletId) {
        this.recipientWalletId = recipientWalletId;
    }

    public String getReceiverFullName() {
        return this.receiverFullName;
    }

    public void setReceiverFullName(String receiverFullName) {
        this.receiverFullName = receiverFullName;
    }

    public String getSenderFullName() {
        return this.senderFullName;
    }

    public void setSenderFullName(String senderFullName) {
        this.senderFullName = senderFullName;
    }

    public String getFullname() {
        return this.fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

}
