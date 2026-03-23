package com.example.admin_api_service.payloads;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.example.admin_api_service.enums.AccountRestrictionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountRestrictionPayload {
    @NotNull(message = "User ID is required")
    private Long userId;
 
    private Long walletId;
 
    @NotNull(message = "Restriction type is required")
    private AccountRestrictionType restrictionType;
 
    @NotBlank(message = "Reason is required")
    @Size(max = 500)
    private String reason;
 
    @Size(max = 500)
    private String internalNote;
 
    private BigDecimal limitAmount;
 
    private String limitCurrency;
 
    @Size(max = 100)
    private String externalReference;
 
    private LocalDateTime expiresAt;
 
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getWalletId() { return walletId; }
    public void setWalletId(Long walletId) { this.walletId = walletId; }
    public AccountRestrictionType getRestrictionType() { return restrictionType; }
    public void setRestrictionType(AccountRestrictionType restrictionType) { this.restrictionType = restrictionType; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getInternalNote() { return internalNote; }
    public void setInternalNote(String internalNote) { this.internalNote = internalNote; }
    public BigDecimal getLimitAmount() { return limitAmount; }
    public void setLimitAmount(BigDecimal limitAmount) { this.limitAmount = limitAmount; }
    public String getLimitCurrency() { return limitCurrency; }
    public void setLimitCurrency(String limitCurrency) { this.limitCurrency = limitCurrency; }
    public String getExternalReference() { return externalReference; }
    public void setExternalReference(String externalReference) { this.externalReference = externalReference; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}
