package pesco.deposit_service.payloads;


import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;
import pesco.deposit_service.enums.CurrencyType;
import pesco.deposit_service.enums.DEPOSITANDWITHDRAWALSYSTEM;
import pesco.deposit_service.enums.TransactionType;

@Data
@Builder
public class DepositRequest {
    private String accountHolderName;
    private String accountNumber;
    private String bankCode;
    private String bankName;
    private Long userId;
    private String email;
    private String username;
    private Long walletId;
    private BigDecimal amount;
    private TransactionType type;
    private CurrencyType currencyType;
    private String currencySymbol;
    private DEPOSITANDWITHDRAWALSYSTEM depositSystem;

    public DepositRequest() {
    }

    public DepositRequest(String accountHolderName, String accountNumber, String bankCode, String bankName, Long userId, String email, String username, Long walletId, BigDecimal amount, TransactionType type, CurrencyType currencyType, String currencySymbol, DEPOSITANDWITHDRAWALSYSTEM depositSystem) {
        this.accountHolderName = accountHolderName;
        this.accountNumber = accountNumber;
        this.bankCode = bankCode;
        this.bankName = bankName;
        this.userId = userId;
        this.email = email;
        this.username = username;
        this.walletId = walletId;
        this.amount = amount;
        this.type = type;
        this.currencyType = currencyType;
        this.currencySymbol = currencySymbol;
        this.depositSystem = depositSystem;
    }


    public String getAccountHolderName() {
        return this.accountHolderName;
    }

    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }

    public String getAccountNumber() {
        return this.accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getBankCode() {
        return this.bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getBankName() {
        return this.bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getWalletId() {
        return this.walletId;
    }

    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
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

    public String getCurrencySymbol() {
        return this.currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public DEPOSITANDWITHDRAWALSYSTEM getDepositSystem() {
        return this.depositSystem;
    }

    public void setDepositSystem(DEPOSITANDWITHDRAWALSYSTEM depositSystem) {
        this.depositSystem = depositSystem;
    }

}
