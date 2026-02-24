package pesco.example.virtual_card_service.requests;

import java.math.BigDecimal;
import pesco.example.virtual_card_service.enums.BalanceOperation;

public class UpdateBalanceRequest {
    private String cardId;
    private BigDecimal amount;
    private BalanceOperation operation;

    public UpdateBalanceRequest() {
    }

    public UpdateBalanceRequest(String cardId, BigDecimal amount, BalanceOperation operation) {
        this.cardId = cardId;
        this.amount = amount;
        this.operation = operation;
    }

    public String getCardId() {
        return this.cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BalanceOperation getOperation() {
        return this.operation;
    }

    public void setOperation(BalanceOperation operation) {
        this.operation = operation;
    }

}
