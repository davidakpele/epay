package pesco.deposit_service.payloads;


import java.math.BigDecimal;

import pesco.deposit_service.enums.CurrencyType;
import pesco.deposit_service.enums.TransactionStatus;
import pesco.deposit_service.enums.TransactionType;

public class HistoryRequest {
    // history payloads
    private Long senderId;
    private Long senderWalletId;
    private Long recipientId;
    private Long recipientWalletId;
    private BigDecimal amount;
    private String description;
    private String message;
    private TransactionStatus status;
    private TransactionType type;
    private CurrencyType currencyType;

    public HistoryRequest() {
    }
    
    public HistoryRequest(Long senderId, Long senderWalletId, Long recipientId, Long recipientWalletId, BigDecimal amount, String description, String message, TransactionStatus status, TransactionType type, CurrencyType currencyType) {
        this.senderId = senderId;
        this.senderWalletId = senderWalletId;
        this.recipientId = recipientId;
        this.recipientWalletId = recipientWalletId;
        this.amount = amount;
        this.description = description;
        this.message = message;
        this.status = status;
        this.type = type;
        this.currencyType = currencyType;
    }

    public Long getSenderId() {
        return this.senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Long getSenderWalletId() {
        return this.senderWalletId;
    }

    public void setSenderWalletId(Long senderWalletId) {
        this.senderWalletId = senderWalletId;
    }

    public Long getRecipientId() {
        return this.recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public Long getRecipientWalletId() {
        return this.recipientWalletId;
    }

    public void setRecipientWalletId(Long recipientWalletId) {
        this.recipientWalletId = recipientWalletId;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
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

    public TransactionStatus getStatus() {
        return this.status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
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

}