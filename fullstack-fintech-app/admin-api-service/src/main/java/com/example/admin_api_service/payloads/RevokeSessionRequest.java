package com.example.admin_api_service.payloads;

import com.example.admin_api_service.enums.SessionTerminationReason;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevokeSessionRequest {
    @NotNull(message = "Termination reason is required")
    private SessionTerminationReason reason;
 
    public SessionTerminationReason getReason() { return reason; }
    public void setReason(SessionTerminationReason reason) { this.reason = reason; }
}
