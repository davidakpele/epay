package com.pesco.wallet_service.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

public class WalletResponseDTO {
    
    private Long id;
    private Long userId;
    private List<CurrencyBalanceDTO> balances;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;
    
    public WalletResponseDTO() {
    }
    
    public WalletResponseDTO(Long id, Long userId, List<CurrencyBalanceDTO> balances, 
                           LocalDateTime createdOn, LocalDateTime updatedOn) {
        this.id = id;
        this.userId = userId;
        this.balances = balances;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
    }
    
    @JsonProperty("id")
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    @JsonProperty("user_id")
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    @JsonProperty("balances")
    public List<CurrencyBalanceDTO> getBalances() {
        return balances;
    }
    
    public void setBalances(List<CurrencyBalanceDTO> balances) {
        this.balances = balances;
    }
    
    @JsonProperty("created_on")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    public LocalDateTime getCreatedOn() {
        return createdOn;
    }
    
    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }
    
    @JsonProperty("updated_on")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    public LocalDateTime getUpdatedOn() {
        return updatedOn;
    }
    
    public void setUpdatedOn(LocalDateTime updatedOn) {
        this.updatedOn = updatedOn;
    }
}