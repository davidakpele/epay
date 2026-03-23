package com.example.admin_api_service.payloads;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.example.admin_api_service.enums.AdjustmentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManualAdjustmentRequest {
    @NotNull(message = "Wallet ID is required")
    private Long walletId;
 
    @NotNull(message = "User ID is required")
    private Long userId;
 
    @NotNull(message = "Adjustment type is required")
    private AdjustmentType adjustmentType;
 
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0001", message = "Amount must be greater than zero")
    private BigDecimal amount;
 
    @NotBlank(message = "Currency is required")
    private String currency;
 
    @NotNull(message = "Balance before is required")
    private BigDecimal balanceBefore;
 
    @NotBlank(message = "Reason is required")
    @jakarta.validation.constraints.Size(max = 500)
    private String reason;
 
    @jakarta.validation.constraints.Size(max = 500)
    private String internalNote;
 
    public Long getWalletId() { return walletId; }
    public void setWalletId(Long walletId) { this.walletId = walletId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public AdjustmentType getAdjustmentType() { return adjustmentType; }
    public void setAdjustmentType(AdjustmentType adjustmentType) { this.adjustmentType = adjustmentType; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public BigDecimal getBalanceBefore() { return balanceBefore; }
    public void setBalanceBefore(BigDecimal balanceBefore) { this.balanceBefore = balanceBefore; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getInternalNote() { return internalNote; }
    public void setInternalNote(String internalNote) { this.internalNote = internalNote; }
}
