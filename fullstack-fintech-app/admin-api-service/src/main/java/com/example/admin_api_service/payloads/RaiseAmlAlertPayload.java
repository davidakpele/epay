package com.example.admin_api_service.payloads;

import java.math.BigDecimal;
import com.example.admin_api_service.enums.AmlAlertSeverity;
import com.example.admin_api_service.enums.AmlAlertType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RaiseAmlAlertPayload {
    @NotNull(message = "User ID is required")
    private Long userId;
 
    private Long walletId;
 
    private String transactionId;
 
    @NotNull(message = "Alert type is required")
    private AmlAlertType alertType;
 
    @NotNull(message = "Severity is required")
    private AmlAlertSeverity severity;
 
    @Size(max = 1000)
    private String description;
 
    private BigDecimal amount;
 
    @Size(max = 10)
    private String currency;
 
    @Size(max = 2000)
    private String evidence;
 
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getWalletId() { return walletId; }
    public void setWalletId(Long walletId) { this.walletId = walletId; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public AmlAlertType getAlertType() { return alertType; }
    public void setAlertType(AmlAlertType alertType) { this.alertType = alertType; }
    public AmlAlertSeverity getSeverity() { return severity; }
    public void setSeverity(AmlAlertSeverity severity) { this.severity = severity; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getEvidence() { return evidence; }
    public void setEvidence(String evidence) { this.evidence = evidence; }
}
