package com.example.admin_api_service.payloads;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevokeIpWhitelistRequest {
    @NotBlank(message = "Revocation reason is required")
    private String reason;
 
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
