package com.example.admin_api_service.Interfaces;

import com.example.admin_api_service.models.accessAndApprovals.ApprovalStep;
import java.util.List;

public interface IApprovalStepService {
    ApprovalStep addStepToWorkflow(String workflowId, ApprovalStep step, String createdBy);
 
    ApprovalStep updateStep(String stepId, ApprovalStep updated, String updatedBy);
 
    ApprovalStep getStepById(String stepId);
 
    List<ApprovalStep> getStepsForWorkflow(String workflowId);
 
    ApprovalStep getNextStep(String workflowId, int currentStepOrder);
 
    ApprovalStep getFirstStep(String workflowId);
 
    void activateStep(String stepId);
 
    void deactivateStep(String stepId);
 
    void deleteStep(String stepId);
 
    void reorderSteps(String workflowId, List<String> orderedStepIds);
}
