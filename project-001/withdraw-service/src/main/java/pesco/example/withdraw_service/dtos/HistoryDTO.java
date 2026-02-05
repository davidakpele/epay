package pesco.example.withdraw_service.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import pesco.example.withdraw_service.enums.CurrencyStructType;
import pesco.example.withdraw_service.enums.TransactionType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HistoryDTO {
    private String id;
    private Long walletId; 
    private Long userId; 
    private String sessionId;
    private BigDecimal amount;

    private TransactionType type;

    private String description;

    private String message;
    
    private CurrencyStructType currencyType; 

    private String status;

    private String ipAddress;

  
    private LocalDateTime createdOn;

    private LocalDateTime updatedOn;

    private OffsetDateTime timestamp;


    public HistoryDTO() {
    }

    public HistoryDTO(String id, Long walletId, Long userId, String sessionId, BigDecimal amount, TransactionType type, String description, String message, CurrencyStructType currencyType, String status, String ipAddress, LocalDateTime createdOn, LocalDateTime updatedOn, OffsetDateTime timestamp) {
        this.id = id;
        this.walletId = walletId;
        this.userId = userId;
        this.sessionId = sessionId;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.message = message;
        this.currencyType = currencyType;
        this.status = status;
        this.ipAddress = ipAddress;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
        this.timestamp = timestamp;
    }


    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getWalletId() {
        return this.walletId;
    }

    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getSessionId() {
        return this.sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
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

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public CurrencyStructType getCurrencyType() {
        return this.currencyType;
    }

    public void setCurrencyType(CurrencyStructType currencyType) {
        this.currencyType = currencyType;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIpAddress() {
        return this.ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public LocalDateTime getCreatedOn() {
        return this.createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public LocalDateTime getUpdatedOn() {
        return this.updatedOn;
    }

    public void setUpdatedOn(LocalDateTime updatedOn) {
        this.updatedOn = updatedOn;
    }

    public OffsetDateTime getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }



}
