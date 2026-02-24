package com.pesco.wallet_service.dtos;

import java.math.BigDecimal;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HistoryDTO {
    private String id;
    
    @JsonProperty("walletId")
    private Long walletId;
    
    @JsonProperty("userId")
    private Long userId;
    
    private String sessionId;
    private BigDecimal amount;
    private String type;
    private String description;
    private String message;
    private String currencyType;
    private String status;
    private String ipAddress;
    private Instant timestamp;
    private Instant createdOn;
    private Instant updatedOn;

    // Default constructor
    public HistoryDTO() {}

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Long getWalletId() { return walletId; }
    public void setWalletId(Long walletId) { this.walletId = walletId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getCurrencyType() { return currencyType; }
    public void setCurrencyType(String currencyType) { this.currencyType = currencyType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public Instant getCreatedOn() { return createdOn; }
    public void setCreatedOn(Instant createdOn) { this.createdOn = createdOn; }

    public Instant getUpdatedOn() { return updatedOn; }
    public void setUpdatedOn(Instant updatedOn) { this.updatedOn = updatedOn; }
}