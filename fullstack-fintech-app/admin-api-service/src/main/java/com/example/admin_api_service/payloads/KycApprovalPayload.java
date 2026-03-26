package com.example.admin_api_service.payloads;

import com.example.admin_api_service.enums.KycTier;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KycApprovalPayload {
    @NotNull(message = "Granted tier is required")
    private KycTier grantedTier;
 
    @Size(max = 500)
    private String reviewNote;
 
    public KycTier getGrantedTier() { return grantedTier; }
    public void setGrantedTier(KycTier grantedTier) { this.grantedTier = grantedTier; }
    public String getReviewNote() { return reviewNote; }
    public void setReviewNote(String reviewNote) { this.reviewNote = reviewNote; }
}
