package com.example.admin_api_service.payloads;

import java.time.LocalDateTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.example.admin_api_service.enums.FreezeReason;
import com.example.admin_api_service.enums.FreezeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletFreezePayload {
    @NotNull(message = "User ID is required")
    private Long userId;
 
    @NotNull(message = "Freeze type is required")
    private FreezeType freezeType;
 
    @NotNull(message = "Freeze reason is required")
    private FreezeReason freezeReason;
 
    @NotBlank(message = "Reason note is required")
    @Size(max = 500)
    private String reasonNote;
 
    @Size(max = 100)
    private String externalReference;
 
    private LocalDateTime expiresAt;
 
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public FreezeType getFreezeType() { return freezeType; }
    public void setFreezeType(FreezeType freezeType) { this.freezeType = freezeType; }
    public FreezeReason getFreezeReason() { return freezeReason; }
    public void setFreezeReason(FreezeReason freezeReason) { this.freezeReason = freezeReason; }
    public String getReasonNote() { return reasonNote; }
    public void setReasonNote(String reasonNote) { this.reasonNote = reasonNote; }
    public String getExternalReference() { return externalReference; }
    public void setExternalReference(String externalReference) { this.externalReference = externalReference; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}
