package pesco.example.withdraw_service.payloads;

import java.math.BigDecimal;
import pesco.example.withdraw_service.enums.CurrencyStructType;

public class UpdateWalletRequest {
    private Long userId;
    private CurrencyStructType currencyType;
    private BigDecimal amount;

    public UpdateWalletRequest(Long userId,  BigDecimal amount, CurrencyStructType currencyType) {
        this.userId = userId;
        this.amount = amount;
        this.currencyType = currencyType;
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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
