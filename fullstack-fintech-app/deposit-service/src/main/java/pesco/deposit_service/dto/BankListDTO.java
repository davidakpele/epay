package pesco.deposit_service.dto;


import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BankListDTO {
    private Long id;
    private String bankCode;
    private String bankName;
    private String accountHolderName;
    private String accountNumber;
    
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Long userId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime createdOn;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime updatedOn;

    // Constructors, getters, and setters

    public BankListDTO() {
    }

    public BankListDTO(Long id, String bankCode, String bankName, String accountHolderName, String accountNumber,
            Long userId, LocalDateTime createdOn, LocalDateTime updatedOn) {
        this.id = id;
        this.bankCode = bankCode;
        this.bankName = bankName;
        this.accountHolderName = accountHolderName;
        this.accountNumber = accountNumber;
        this.userId = userId;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public LocalDateTime getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(LocalDateTime updatedOn) {
        this.updatedOn = updatedOn;
    }
}