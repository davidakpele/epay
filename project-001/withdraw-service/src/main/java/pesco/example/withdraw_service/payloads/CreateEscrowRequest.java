package pesco.example.withdraw_service.payloads;

import java.math.BigDecimal;

public class CreateEscrowRequest {
    private Long senderId;
    private Long recipientId;
    private String description;
    private String currencyCode;
    private BigDecimal amount;

    public CreateEscrowRequest() {
    }

    public CreateEscrowRequest(Long senderId, Long recipientId, String description, String currencyCode, BigDecimal amount) {
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.description = description;
        this.currencyCode = currencyCode;
        this.amount = amount;
    }

    public Long getSenderId() {
        return this.senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Long getRecipientId() {
        return this.recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCurrencyCode() {
        return this.currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

}
