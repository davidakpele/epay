package com.example.admin_api_service.payloads;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluateTransactionPayload {
    @NotNull(message = "User ID is required")
    private Long userId;
 
    private Long walletId;
 
    private String transactionId;
 
    @Size(max = 5000)
    private String inputContextJson;
 
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getWalletId() { return walletId; }
    public void setWalletId(Long walletId) { this.walletId = walletId; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public String getInputContextJson() { return inputContextJson; }
    public void setInputContextJson(String inputContextJson) { this.inputContextJson = inputContextJson; }
}
