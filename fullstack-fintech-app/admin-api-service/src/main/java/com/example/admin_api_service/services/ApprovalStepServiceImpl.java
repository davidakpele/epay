package com.example.admin_api_service.services;

import com.example.admin_api_service.Interfaces.IApprovalStepService;
import com.example.admin_api_service.Interfaces.IApprovalWorkflowService;
import com.example.admin_api_service.enums.ApprovalStepStatus;
import com.example.admin_api_service.exceptions.BadRequestException;
import com.example.admin_api_service.exceptions.ResourceNotFoundException;
import com.example.admin_api_service.models.accessAndApprovals.ApprovalStep;
import com.example.admin_api_service.repository.ApprovalStepRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Transactional
public class ApprovalStepServiceImpl implements IApprovalStepService {

    private final ApprovalStepRepository stepRepository;
    private final IApprovalWorkflowService workflowService;

    public ApprovalStepServiceImpl(ApprovalStepRepository stepRepository,
                                   IApprovalWorkflowService workflowService) {
        this.stepRepository = stepRepository;
        this.workflowService = workflowService;
    }

    @Override
    public ApprovalStep addStepToWorkflow(String workflowId, ApprovalStep step, String createdBy) {
        workflowService.getWorkflowById(workflowId); // validate workflow exists

        int nextOrder = stepRepository.countByWorkflowId(workflowId) + 1;
        step.setWorkflowId(workflowId);
        step.setStepOrder(nextOrder);
        step.setStatus(ApprovalStepStatus.ACTIVE);
        return stepRepository.save(step);
    }

    @Override
    public ApprovalStep updateStep(String stepId, ApprovalStep updated, String updatedBy) {
        ApprovalStep existing = getStepById(stepId);
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setRequiredRoleId(updated.getRequiredRoleId());
        existing.setAssignedTo(updated.getAssignedTo());
        existing.setRequiredApprovers(updated.getRequiredApprovers());
        existing.setBlocking(updated.isBlocking());
        existing.setUpdatedOn(LocalDateTime.now());
        return stepRepository.save(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public ApprovalStep getStepById(String stepId) {
        return stepRepository.findById(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("ApprovalStep", "id", stepId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApprovalStep> getStepsForWorkflow(String workflowId) {
        return stepRepository.findAllByWorkflowIdOrderByStepOrderAsc(workflowId);
    }

    @Override
    @Transactional(readOnly = true)
    public ApprovalStep getNextStep(String workflowId, int currentStepOrder) {
        return stepRepository
                .findFirstByWorkflowIdAndStepOrderGreaterThanAndStatusOrderByStepOrderAsc(
                        workflowId, currentStepOrder, ApprovalStepStatus.ACTIVE)
                .orElse(null); // null = no more steps, request is fully approved
    }

    @Override
    @Transactional(readOnly = true)
    public ApprovalStep getFirstStep(String workflowId) {
        return stepRepository
                .findFirstByWorkflowIdAndStatusOrderByStepOrderAsc(workflowId, ApprovalStepStatus.ACTIVE)
                .orElseThrow(() -> new BadRequestException(
                        "Workflow '" + workflowId + "' has no active steps configured"));
    }

    @Override
    public void activateStep(String stepId) {
        ApprovalStep step = getStepById(stepId);
        step.setStatus(ApprovalStepStatus.ACTIVE);
        step.setUpdatedOn(LocalDateTime.now());
        stepRepository.save(step);
    }

    @Override
    public void deactivateStep(String stepId) {
        ApprovalStep step = getStepById(stepId);
        step.setStatus(ApprovalStepStatus.INACTIVE);
        step.setUpdatedOn(LocalDateTime.now());
        stepRepository.save(step);
    }

    @Override
    public void deleteStep(String stepId) {
        ApprovalStep step = getStepById(stepId);
        stepRepository.delete(step);
        // Re-sequence remaining steps
        List<ApprovalStep> remaining = stepRepository
                .findAllByWorkflowIdOrderByStepOrderAsc(step.getWorkflowId());
        AtomicInteger order = new AtomicInteger(1);
        remaining.forEach(s -> s.setStepOrder(order.getAndIncrement()));
        stepRepository.saveAll(remaining);
    }

    @Override
    public void reorderSteps(String workflowId, List<String> orderedStepIds) {
        List<ApprovalStep> steps = stepRepository.findAllByWorkflowIdOrderByStepOrderAsc(workflowId);
        if (steps.size() != orderedStepIds.size()) {
            throw new BadRequestException("Ordered step IDs count does not match existing steps count");
        }
        for (int i = 0; i < orderedStepIds.size(); i++) {
            final int order = i + 1;
            final String stepId = orderedStepIds.get(i);
            steps.stream()
                    .filter(s -> s.getId().equals(stepId))
                    .findFirst()
                    .ifPresent(s -> s.setStepOrder(order));
        }
        stepRepository.saveAll(steps);
    }
}