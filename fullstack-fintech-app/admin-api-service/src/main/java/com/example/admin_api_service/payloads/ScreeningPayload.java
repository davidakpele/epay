package com.example.admin_api_service.payloads;

import com.example.admin_api_service.enums.SanctionScreeningTrigger;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScreeningPayload {
    @NotNull(message = "User ID is required")
    private Long userId;
 
    private Long walletId;
 
    private String transactionId;
 
    @NotNull(message = "Trigger is required")
    private SanctionScreeningTrigger trigger;
 
    @NotBlank(message = "Name to screen is required")
    @Size(max = 200)
    private String screenedName;
 
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getWalletId() { return walletId; }
    public void setWalletId(Long walletId) { this.walletId = walletId; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public SanctionScreeningTrigger getTrigger() { return trigger; }
    public void setTrigger(SanctionScreeningTrigger trigger) { this.trigger = trigger; }
    public String getScreenedName() { return screenedName; }
    public void setScreenedName(String screenedName) { this.screenedName = screenedName; }
}
