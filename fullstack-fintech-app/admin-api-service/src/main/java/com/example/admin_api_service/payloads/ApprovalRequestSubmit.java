package com.example.admin_api_service.payloads;

import java.math.BigDecimal;
import com.example.admin_api_service.enums.ApprovalTargetType;
import com.example.admin_api_service.enums.ApprovalWorkflowType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRequestSubmit {
    @NotNull(message = "Workflow type is required")
    private ApprovalWorkflowType workflowType;
 
    @NotNull(message = "Target type is required")
    private ApprovalTargetType targetType;
 
    @NotBlank(message = "Target ID is required")
    private String targetId;
 
    private BigDecimal amount;
 
    private String currency;
 
    private String payload;
 
    @NotBlank(message = "Request note is required")
    private String requestNote;
 
    public ApprovalWorkflowType getWorkflowType() { return workflowType; }
    public void setWorkflowType(ApprovalWorkflowType workflowType) { this.workflowType = workflowType; }
    public ApprovalTargetType getTargetType() { return targetType; }
    public void setTargetType(ApprovalTargetType targetType) { this.targetType = targetType; }
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public String getRequestNote() { return requestNote; }
    public void setRequestNote(String requestNote) { this.requestNote = requestNote; }
}
