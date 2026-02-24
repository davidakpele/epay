package pesco.example.withdraw_service.payloads;

import java.math.BigDecimal;
import pesco.example.withdraw_service.enums.CurrencyStructType;

public class DeductAmountRequest {
    private Long id;
    private CurrencyStructType currencyType;
    private BigDecimal amount;

    public DeductAmountRequest(Long id, CurrencyStructType currencyType, BigDecimal amount) {
        this.id = id;
        this.currencyType = currencyType;
        this.amount = amount;
    }


    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CurrencyStructType getCurrencyType() {
        return this.currencyType;
    }

    public void setCurrencyType(CurrencyStructType currencyType) {
        this.currencyType = currencyType;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }


}
