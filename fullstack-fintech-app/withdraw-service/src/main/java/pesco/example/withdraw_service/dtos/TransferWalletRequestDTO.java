package pesco.example.withdraw_service.dtos;

import java.math.BigDecimal;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class TransferWalletRequestDTO {
    @NotBlank(message = "Username is required")
    private String username;
    
    @NotBlank(message = "Account number is required")
    @Pattern(regexp = "\\d{10,16}", message = "Account number must be 10-16 digits")
    private String accountNumber;
    
    @NotBlank(message = "Bank code is required")
    private String bankCode;
    
    @NotBlank(message = "Account name is required")
    @Size(min = 2, max = 100, message = "Account name must be between 2 and 100 characters")
    private String accountName;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    @NotBlank(message = "Currency is required")
    private String currency;

    @NotBlank(message = "Wallet Id is required")
    private Long walletId;

    @NotBlank(message = "Transfer Pin is required")
    private String password;

    @NotBlank(message = "Sender Id is required")
    @NotNull(message = "Sender is required")
    private Long senderUserId;

    @NotBlank(message = "Idempotency key is required")
    @NotNull(message = "Idempotency key is required")
    private String idempotencyKey;

    public TransferWalletRequestDTO(String username, String accountNumber, String bankCode, String accountName, BigDecimal amount, String currency, Long walletId, String password, Long senderUserId, String idempotencyKey) {
        this.username = username;
        this.accountNumber = accountNumber;
        this.bankCode = bankCode;
        this.accountName = accountName;
        this.amount = amount;
        this.currency = currency;
        this.walletId = walletId;
        this.password = password;
        this.senderUserId = senderUserId;
        this.idempotencyKey = idempotencyKey;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getAccountName() {
        return this.accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return this.currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Long getWalletId() {
        return this.walletId;
    }

    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getSenderUserId() {
        return this.senderUserId;
    }

    public void setSenderUserId(Long senderUserId) {
        this.senderUserId = senderUserId;
    }

    public String getIdempotencyKey() {
        return this.idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
    
}