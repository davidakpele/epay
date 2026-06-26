package com.example.admin_api_service.payloads;

import java.math.BigDecimal;
import com.example.admin_api_service.enums.ApprovalWorkflowType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalWorkflowRequest {
    @NotBlank(message = "Workflow name is required")
    @Size(max = 100)
    private String name;
 
    @Size(max = 255)
    private String description;
 
    @NotNull(message = "Workflow type is required")
    private ApprovalWorkflowType type;
 
    @Min(value = 1, message = "Minimum approvers must be at least 1")
    private int minApprovers = 1;
 
    private BigDecimal amountThreshold;
 
    private Integer expiryHours;
 
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public ApprovalWorkflowType getType() { return type; }
    public void setType(ApprovalWorkflowType type) { this.type = type; }
    public int getMinApprovers() { return minApprovers; }
    public void setMinApprovers(int minApprovers) { this.minApprovers = minApprovers; }
    public BigDecimal getAmountThreshold() { return amountThreshold; }
    public void setAmountThreshold(BigDecimal amountThreshold) { this.amountThreshold = amountThreshold; }
    public Integer getExpiryHours() { return expiryHours; }
    public void setExpiryHours(Integer expiryHours) { this.expiryHours = expiryHours; }
}
