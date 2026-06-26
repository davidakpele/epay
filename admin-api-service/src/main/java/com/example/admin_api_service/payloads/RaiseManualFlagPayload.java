package com.example.admin_api_service.payloads;

import com.example.admin_api_service.enums.AccountFlagSeverity;
import com.example.admin_api_service.enums.AccountFlagType;
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
public class RaiseManualFlagPayload {
    @NotNull(message = "User ID is required")
    private Long userId;
 
    private Long walletId;
 
    @NotNull(message = "Flag type is required")
    private AccountFlagType flagType;
 
    @NotNull(message = "Severity is required")
    private AccountFlagSeverity severity;
 
    @NotBlank(message = "Title is required")
    @Size(max = 200)
    private String title;
 
    @Size(max = 1000)
    private String description;
 
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getWalletId() { return walletId; }
    public void setWalletId(Long walletId) { this.walletId = walletId; }
    public AccountFlagType getFlagType() { return flagType; }
    public void setFlagType(AccountFlagType flagType) { this.flagType = flagType; }
    public AccountFlagSeverity getSeverity() { return severity; }
    public void setSeverity(AccountFlagSeverity severity) { this.severity = severity; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
