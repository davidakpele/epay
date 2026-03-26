package com.example.admin_api_service.payloads;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
 import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmitComplianceReportPayload {
 
    @NotBlank(message = "Submission reference is required")
    @Size(max = 100)
    private String submissionReference;
 
    public String getSubmissionReference() { return submissionReference; }
    public void setSubmissionReference(String submissionReference) { this.submissionReference = submissionReference; }
}
