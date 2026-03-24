package com.example.admin_api_service.payloads;

import com.example.admin_api_service.enums.SanctionScreeningResult;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewScreeningPayload {
    @Size(max = 500)
    private String reviewNote;
 
    private SanctionScreeningResult overrideResult;
 
    public String getReviewNote() { return reviewNote; }
    public void setReviewNote(String reviewNote) { this.reviewNote = reviewNote; }
    public SanctionScreeningResult getOverrideResult() { return overrideResult; }
    public void setOverrideResult(SanctionScreeningResult overrideResult) { this.overrideResult = overrideResult; }
}
