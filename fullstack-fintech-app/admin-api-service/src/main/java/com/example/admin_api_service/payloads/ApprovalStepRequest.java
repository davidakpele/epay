package com.example.admin_api_service.payloads;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalStepRequest {

    @NotBlank(message = "Step name is required")
    @Size(max = 100)
    private String name;
 
    @Size(max = 255)
    private String description;
 
    private String requiredRoleId;
 
    private String assignedTo;
 
    @Min(value = 1, message = "Required approvers must be at least 1")
    private int requiredApprovers = 1;
 
    private boolean isBlocking = true;
 
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getRequiredRoleId() { return requiredRoleId; }
    public void setRequiredRoleId(String requiredRoleId) { this.requiredRoleId = requiredRoleId; }
    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
    public int getRequiredApprovers() { return requiredApprovers; }
    public void setRequiredApprovers(int requiredApprovers) { this.requiredApprovers = requiredApprovers; }
    public boolean isBlocking() { return isBlocking; }
    public void setBlocking(boolean blocking) { isBlocking = blocking; }
}
