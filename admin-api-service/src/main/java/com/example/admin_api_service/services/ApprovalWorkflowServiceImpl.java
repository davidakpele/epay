package com.example.admin_api_service.services;

import com.example.admin_api_service.Interfaces.IApprovalWorkflowService;
import com.example.admin_api_service.enums.ApprovalWorkflowStatus;
import com.example.admin_api_service.enums.ApprovalWorkflowType;
import com.example.admin_api_service.exceptions.ConflictException;
import com.example.admin_api_service.exceptions.ResourceNotFoundException;
import com.example.admin_api_service.models.accessAndApprovals.ApprovalWorkflow;
import com.example.admin_api_service.repository.ApprovalWorkflowRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional
public class ApprovalWorkflowServiceImpl implements IApprovalWorkflowService {

    private final ApprovalWorkflowRepository workflowRepository;

    public ApprovalWorkflowServiceImpl(ApprovalWorkflowRepository workflowRepository) {
        this.workflowRepository = workflowRepository;
    }

    @Override
    public ApprovalWorkflow createWorkflow(ApprovalWorkflow workflow, String createdBy) {
        if (workflowRepository.existsByName(workflow.getName())) {
            throw new ConflictException("Workflow with name '" + workflow.getName() + "' already exists");
        }
        workflow.setCreatedBy(createdBy);
        workflow.setStatus(ApprovalWorkflowStatus.ACTIVE);
        workflow.setActive(true);
        return workflowRepository.save(workflow);
    }

    @Override
    public ApprovalWorkflow updateWorkflow(String workflowId, ApprovalWorkflow updated, String updatedBy) {
        ApprovalWorkflow existing = getWorkflowById(workflowId);

        if (!existing.getName().equals(updated.getName())
                && workflowRepository.existsByName(updated.getName())) {
            throw new ConflictException("Workflow with name '" + updated.getName() + "' already exists");
        }

        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setType(updated.getType());
        existing.setMinApprovers(updated.getMinApprovers());
        existing.setAmountThreshold(updated.getAmountThreshold());
        existing.setExpiryHours(updated.getExpiryHours());
        existing.setUpdatedBy(updatedBy);
        existing.setUpdatedOn(LocalDateTime.now());
        return workflowRepository.save(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public ApprovalWorkflow getWorkflowById(String workflowId) {
        return workflowRepository.findById(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("ApprovalWorkflow", "id", workflowId));
    }

    @Override
    @Transactional(readOnly = true)
    public ApprovalWorkflow getWorkflowByName(String name) {
        return workflowRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("ApprovalWorkflow", "name", name));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ApprovalWorkflow> getAllWorkflows(Pageable pageable) {
        return workflowRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApprovalWorkflow> getWorkflowsByType(ApprovalWorkflowType type) {
        return workflowRepository.findAllByTypeAndIsActiveTrue(type);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApprovalWorkflow> getActiveWorkflows() {
        return workflowRepository.findAllByIsActiveTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public ApprovalWorkflow resolveWorkflow(ApprovalWorkflowType type, BigDecimal amount) {
        // Find active workflows for the type whose threshold the amount meets or exceeds
        // Pick the one with the highest threshold that the amount still satisfies
        return workflowRepository.findAllByTypeAndIsActiveTrue(type).stream()
                .filter(w -> w.getAmountThreshold() == null
                        || amount.compareTo(w.getAmountThreshold()) >= 0)
                .max(Comparator.comparing(w -> w.getAmountThreshold() == null
                        ? BigDecimal.ZERO : w.getAmountThreshold()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ApprovalWorkflow", "type", type.name()));
    }

    @Override
    public void activateWorkflow(String workflowId, String updatedBy) {
        ApprovalWorkflow workflow = getWorkflowById(workflowId);
        workflow.setActive(true);
        workflow.setStatus(ApprovalWorkflowStatus.ACTIVE);
        workflow.setUpdatedBy(updatedBy);
        workflow.setUpdatedOn(LocalDateTime.now());
        workflowRepository.save(workflow);
    }

    @Override
    public void deactivateWorkflow(String workflowId, String updatedBy) {
        ApprovalWorkflow workflow = getWorkflowById(workflowId);
        workflow.setActive(false);
        workflow.setStatus(ApprovalWorkflowStatus.INACTIVE);
        workflow.setUpdatedBy(updatedBy);
        workflow.setUpdatedOn(LocalDateTime.now());
        workflowRepository.save(workflow);
    }

    @Override
    public void deleteWorkflow(String workflowId) {
        ApprovalWorkflow workflow = getWorkflowById(workflowId);
        if (!workflow.getRequests().isEmpty()) {
            throw new ConflictException("Workflow has existing approval requests and cannot be deleted. Deactivate it instead.");
        }
        workflowRepository.delete(workflow);
    }
}