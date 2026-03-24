package com.example.admin_api_service.payloads;

import jakarta.validation.constraints.Size;
 import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewComplianceReportPayload {
    @Size(max = 1000)
    private String reviewNote;
 
    public String getReviewNote() { return reviewNote; }
    public void setReviewNote(String reviewNote) { this.reviewNote = reviewNote; }
}