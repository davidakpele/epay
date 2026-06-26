package com.example.admin_api_service.models.accessAndApprovals;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.admin_api_service.enums.ApprovalWorkflowStatus;
import com.example.admin_api_service.enums.ApprovalWorkflowType;

@Entity
@Table(name = "approval_workflows")
public class ApprovalWorkflow {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id = UUID.randomUUID().toString();

    @Column(name = "name", length = 100, nullable = false, unique = true)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    // What kind of operation this workflow governs
    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 50, nullable = false)
    private ApprovalWorkflowType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private ApprovalWorkflowStatus status = ApprovalWorkflowStatus.ACTIVE;

    // Minimum number of approvers required across all steps
    @Column(name = "min_approvers", nullable = false)
    private int minApprovers = 1;

    // Threshold above which this workflow is triggered (e.g. amount > 1,000,000)
    @Column(name = "amount_threshold", precision = 18, scale = 4)
    private java.math.BigDecimal amountThreshold;

    // Auto-expire pending requests after N hours
    @Column(name = "expiry_hours")
    private Integer expiryHours;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_by", length = 36)
    private String createdBy;

    @Column(name = "updated_by", length = 36)
    private String updatedBy;

    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn = LocalDateTime.now();

    @Column(name = "updated_on", nullable = false)
    private LocalDateTime updatedOn = LocalDateTime.now();

    // Navigation
    @OneToMany(mappedBy = "workflow", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stepOrder ASC")
    private List<ApprovalStep> steps = new ArrayList<>();

    @OneToMany(mappedBy = "workflow")
    private List<ApprovalRequest> requests = new ArrayList<>();

    @PreUpdate
    public void preUpdate() {
        this.updatedOn = LocalDateTime.now();
    }

    public ApprovalWorkflow() {
    }

    public ApprovalWorkflow(String id, String name, String description, ApprovalWorkflowType type, ApprovalWorkflowStatus status, int minApprovers, java.math.BigDecimal amountThreshold, Integer expiryHours, boolean isActive, String createdBy, String updatedBy, LocalDateTime createdOn, LocalDateTime updatedOn, List<ApprovalStep> steps, List<ApprovalRequest> requests) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.status = status;
        this.minApprovers = minApprovers;
        this.amountThreshold = amountThreshold;
        this.expiryHours = expiryHours;
        this.isActive = isActive;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
        this.steps = steps;
        this.requests = requests;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ApprovalWorkflowType getType() { return type; }
    public void setType(ApprovalWorkflowType type) { this.type = type; }

    public ApprovalWorkflowStatus getStatus() { return status; }
    public void setStatus(ApprovalWorkflowStatus status) { this.status = status; }

    public int getMinApprovers() { return minApprovers; }
    public void setMinApprovers(int minApprovers) { this.minApprovers = minApprovers; }

    public java.math.BigDecimal getAmountThreshold() { return amountThreshold; }
    public void setAmountThreshold(java.math.BigDecimal amountThreshold) { this.amountThreshold = amountThreshold; }

    public Integer getExpiryHours() { return expiryHours; }
    public void setExpiryHours(Integer expiryHours) { this.expiryHours = expiryHours; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }

    public LocalDateTime getUpdatedOn() { return updatedOn; }
    public void setUpdatedOn(LocalDateTime updatedOn) { this.updatedOn = updatedOn; }

    public List<ApprovalStep> getSteps() { return steps; }
    public void setSteps(List<ApprovalStep> steps) { this.steps = steps; }

    public List<ApprovalRequest> getRequests() { return requests; }
    public void setRequests(List<ApprovalRequest> requests) { this.requests = requests; }
}
