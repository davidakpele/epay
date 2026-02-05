package pesco.example.withdraw_service.payloads;

import java.math.BigDecimal;
import lombok.Data;
import pesco.example.withdraw_service.enums.CurrencyStructType;

@Data
public class WithdrawOutRequest {
    private String senderUser;
    private String accountNumber;
    private BigDecimal amount;
    private String bankcode;
    private String description;
    private String recipientUser;
    private CurrencyStructType currencyType;
    private String transferpin;

    public WithdrawOutRequest(){}
    
    public WithdrawOutRequest(String senderUser, String accountNumber, BigDecimal amount, String bankcode, String description, String recipientUser, 
            CurrencyStructType currencyType, String transferpin) {
        this.senderUser = senderUser;
        this.accountNumber = accountNumber;
        this.amount = amount;
        this.bankcode = bankcode;
        this.description = description;
        this.recipientUser = recipientUser;
        this.currencyType = currencyType;
        this.transferpin = transferpin;
    }

    public String getSenderUser() {
        return this.senderUser;
    }

    public void setSenderUser(String senderUser) {
        this.senderUser = senderUser;
    }

    public String getAccountNumber() {
        return this.accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getBankcode() {
        return this.bankcode;
    }

    public void setBankcode(String bankcode) {
        this.bankcode = bankcode;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRecipientUser() {
        return this.recipientUser;
    }

    public void setRecipientUser(String recipientUser) {
        this.recipientUser = recipientUser;
    }

    public CurrencyStructType getCurrencyType() {
        return this.currencyType;
    }

    public void setCurrencyType(CurrencyStructType currencyType) {
        this.currencyType = currencyType;
    }

    public String getTransferpin() {
        return this.transferpin;
    }

    public void setTransferpin(String transferpin) {
        this.transferpin = transferpin;
    }
    

}
