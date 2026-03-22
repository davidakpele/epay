package com.example.admin_api_service.models.accessAndApprovals;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.admin_api_service.enums.ApprovalStepStatus;
import com.example.admin_api_service.models.accessAndSecurity.AdminRole;

@Entity
@Table(name = "approval_steps")
public class ApprovalStep {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id = UUID.randomUUID().toString();

    @Column(name = "workflow_id", length = 36, nullable = false)
    private String workflowId;

    // Which step in sequence (1, 2, 3...)
    @Column(name = "step_order", nullable = false)
    private int stepOrder;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    // Role required to action this step
    @Column(name = "required_role_id", length = 36)
    private String requiredRoleId;

    // Specific admin assigned (optional — if null, any admin with the role can act)
    @Column(name = "assigned_to", length = 36)
    private String assignedTo;

    // How many approvers needed at this step
    @Column(name = "required_approvers", nullable = false)
    private int requiredApprovers = 1;

    // If true, rejection at this step cancels the entire request
    @Column(name = "is_blocking", nullable = false)
    private boolean isBlocking = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private ApprovalStepStatus status = ApprovalStepStatus.ACTIVE;

    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn = LocalDateTime.now();

    @Column(name = "updated_on", nullable = false)
    private LocalDateTime updatedOn = LocalDateTime.now();

    // Navigation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", insertable = false, updatable = false)
    private ApprovalWorkflow workflow;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "required_role_id", insertable = false, updatable = false)
    private AdminRole requiredRole;

    @PreUpdate
    public void preUpdate() {
        this.updatedOn = LocalDateTime.now();
    }

    public ApprovalStep() {
    }

    public ApprovalStep(String id, String workflowId, int stepOrder, String name, String description, String requiredRoleId, String assignedTo, int requiredApprovers, boolean isBlocking, ApprovalStepStatus status, LocalDateTime createdOn, LocalDateTime updatedOn, ApprovalWorkflow workflow, AdminRole requiredRole) {
        this.id = id;
        this.workflowId = workflowId;
        this.stepOrder = stepOrder;
        this.name = name;
        this.description = description;
        this.requiredRoleId = requiredRoleId;
        this.assignedTo = assignedTo;
        this.requiredApprovers = requiredApprovers;
        this.isBlocking = isBlocking;
        this.status = status;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
        this.workflow = workflow;
        this.requiredRole = requiredRole;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getWorkflowId() { return workflowId; }
    public void setWorkflowId(String workflowId) { this.workflowId = workflowId; }

    public int getStepOrder() { return stepOrder; }
    public void setStepOrder(int stepOrder) { this.stepOrder = stepOrder; }

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

    public ApprovalStepStatus getStatus() { return status; }
    public void setStatus(ApprovalStepStatus status) { this.status = status; }

    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }

    public LocalDateTime getUpdatedOn() { return updatedOn; }
    public void setUpdatedOn(LocalDateTime updatedOn) { this.updatedOn = updatedOn; }

    public ApprovalWorkflow getWorkflow() { return workflow; }
    public void setWorkflow(ApprovalWorkflow workflow) { this.workflow = workflow; }

    public AdminRole getRequiredRole() { return requiredRole; }
    public void setRequiredRole(AdminRole requiredRole) { this.requiredRole = requiredRole; }
}
