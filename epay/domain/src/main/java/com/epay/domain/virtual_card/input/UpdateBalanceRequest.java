package com.epay.domain.virtual_card.input;

import java.math.BigDecimal;
import com.epay.domain.virtual_card.enums.BalanceOperation;

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
