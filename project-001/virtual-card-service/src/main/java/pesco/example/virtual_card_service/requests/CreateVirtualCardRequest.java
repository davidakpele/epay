package pesco.example.virtual_card_service.requests;

import java.math.BigDecimal;
import pesco.example.virtual_card_service.enums.CardPlan;
import pesco.example.virtual_card_service.enums.CardType;
import pesco.example.virtual_card_service.enums.LimitPeriod;

public class CreateVirtualCardRequest {
    private Long userId;
    private String accountHolderName;
    private CardType cardType;
    private String currency;
    private BigDecimal initialBalance;
    private BigDecimal spendingLimit;
    private LimitPeriod limitPeriod;
    private CardPlan plan;

    public CreateVirtualCardRequest() {
    }

    public CreateVirtualCardRequest(Long userId, String accountHolderName, CardType cardType, String currency, BigDecimal initialBalance, BigDecimal spendingLimit, LimitPeriod limitPeriod, CardPlan plan) {
        this.userId = userId;
        this.accountHolderName = accountHolderName;
        this.cardType = cardType;
        this.currency = currency;
        this.initialBalance = initialBalance;
        this.spendingLimit = spendingLimit;
        this.limitPeriod = limitPeriod;
        this.plan = plan;
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getAccountHolderName() {
        return this.accountHolderName;
    }

    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }

    public CardType getCardType() {
        return this.cardType;
    }

    public void setCardType(CardType cardType) {
        this.cardType = cardType;
    }

    public String getCurrency() {
        return this.currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getInitialBalance() {
        return this.initialBalance;
    }

    public void setInitialBalance(BigDecimal initialBalance) {
        this.initialBalance = initialBalance;
    }

    public BigDecimal getSpendingLimit() {
        return this.spendingLimit;
    }

    public void setSpendingLimit(BigDecimal spendingLimit) {
        this.spendingLimit = spendingLimit;
    }

    public LimitPeriod getLimitPeriod() {
        return this.limitPeriod;
    }

    public void setLimitPeriod(LimitPeriod limitPeriod) {
        this.limitPeriod = limitPeriod;
    }

    public CardPlan getPlan() {
        return this.plan;
    }

    public void setPlan(CardPlan plan) {
        this.plan = plan;
    }

}
