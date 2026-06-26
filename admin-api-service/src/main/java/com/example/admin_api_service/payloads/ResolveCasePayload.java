package com.example.admin_api_service.payloads;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResolveCasePayload {
    private boolean suspiciousActivityConfirmed;
 
    @NotBlank(message = "Closure note is required")
    @Size(max = 1000)
    private String closureNote;
 
    public boolean isSuspiciousActivityConfirmed() { return suspiciousActivityConfirmed; }
    public void setSuspiciousActivityConfirmed(boolean suspiciousActivityConfirmed) { this.suspiciousActivityConfirmed = suspiciousActivityConfirmed; }
    public String getClosureNote() { return closureNote; }
    public void setClosureNote(String closureNote) { this.closureNote = closureNote; }
}
