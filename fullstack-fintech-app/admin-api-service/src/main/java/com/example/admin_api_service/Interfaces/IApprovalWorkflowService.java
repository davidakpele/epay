package com.example.admin_api_service.Interfaces;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.admin_api_service.enums.ApprovalWorkflowType;
import com.example.admin_api_service.models.accessAndApprovals.ApprovalWorkflow;

public interface IApprovalWorkflowService {
    ApprovalWorkflow createWorkflow(ApprovalWorkflow workflow, String createdBy);
 
    ApprovalWorkflow updateWorkflow(String workflowId, ApprovalWorkflow updated, String updatedBy);
 
    ApprovalWorkflow getWorkflowById(String workflowId);
 
    ApprovalWorkflow getWorkflowByName(String name);
 
    Page<ApprovalWorkflow> getAllWorkflows(Pageable pageable);
 
    List<ApprovalWorkflow> getWorkflowsByType(ApprovalWorkflowType type);
 
    List<ApprovalWorkflow> getActiveWorkflows();
 
    // Resolve which workflow applies for a given type and amount
    ApprovalWorkflow resolveWorkflow(ApprovalWorkflowType type, BigDecimal amount);
 
    void activateWorkflow(String workflowId, String updatedBy);
 
    void deactivateWorkflow(String workflowId, String updatedBy);
 
    void deleteWorkflow(String workflowId);
}
