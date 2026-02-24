package pesco.deposit_service.payloads;


import java.math.BigDecimal;

import pesco.deposit_service.enums.CurrencyType;

public class CreditWalletRequest {
    private BigDecimal amount;
    private CurrencyType currencyType;
    private Long userId;
   


    public CreditWalletRequest(BigDecimal amount, CurrencyType currencyType, Long userId) {
        this.amount = amount;
        this.currencyType = currencyType;
        this.userId = userId;
    }


    public BigDecimal getAmount() {
        return this.amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public CurrencyType getCurrencyType() {
        return this.currencyType;
    }

    public void setCurrencyType(CurrencyType currencyType) {
        this.currencyType = currencyType;
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

}
