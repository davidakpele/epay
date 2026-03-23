package com.example.admin_api_service.controllers;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.example.admin_api_service.Interfaces.IApprovalStepService;
import com.example.admin_api_service.Interfaces.IApprovalWorkflowService;
import com.example.admin_api_service.dto.ApiResponse;
import com.example.admin_api_service.enums.ApprovalWorkflowType;
import com.example.admin_api_service.models.accessAndApprovals.ApprovalStep;
import com.example.admin_api_service.models.accessAndApprovals.ApprovalWorkflow;
import com.example.admin_api_service.payloads.ApprovalStepRequest;
import com.example.admin_api_service.payloads.ApprovalWorkflowRequest;
import com.example.admin_api_service.payloads.ReorderStepsRequest;
import java.util.List;

@RestController
@RequestMapping("/admin/approval-workflows")
public class ApprovalWorkflowController {

    private final IApprovalWorkflowService workflowService;
    private final IApprovalStepService stepService;

    public ApprovalWorkflowController(IApprovalWorkflowService workflowService,
                                       IApprovalStepService stepService) {
        this.workflowService = workflowService;
        this.stepService = stepService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ApprovalWorkflow>> createWorkflow(
            @Valid @RequestBody ApprovalWorkflowRequest request,
            @AuthenticationPrincipal String adminId) {

        ApprovalWorkflow workflow = mapToWorkflow(request);
        ApprovalWorkflow created = workflowService.createWorkflow(workflow, adminId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Workflow created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ApprovalWorkflow>>> getAllWorkflows(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<ApprovalWorkflow> workflows = workflowService.getAllWorkflows(
                PageRequest.of(page, size, Sort.by("createdOn").descending()));
        return ResponseEntity.ok(ApiResponse.success(workflows));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<ApprovalWorkflow>>> getActiveWorkflows() {
        return ResponseEntity.ok(ApiResponse.success(workflowService.getActiveWorkflows()));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<List<ApprovalWorkflow>>> getWorkflowsByType(
            @PathVariable ApprovalWorkflowType type) {
        return ResponseEntity.ok(ApiResponse.success(workflowService.getWorkflowsByType(type)));
    }

    @GetMapping("/{workflowId}")
    public ResponseEntity<ApiResponse<ApprovalWorkflow>> getWorkflowById(
            @PathVariable String workflowId) {
        return ResponseEntity.ok(ApiResponse.success(workflowService.getWorkflowById(workflowId)));
    }

    @PutMapping("/{workflowId}")
    public ResponseEntity<ApiResponse<ApprovalWorkflow>> updateWorkflow(
            @PathVariable String workflowId,
            @Valid @RequestBody ApprovalWorkflowRequest request,
            @AuthenticationPrincipal String adminId) {

        ApprovalWorkflow updated = workflowService.updateWorkflow(
                workflowId, mapToWorkflow(request), adminId);
        return ResponseEntity.ok(ApiResponse.success(updated, "Workflow updated"));
    }

    @PatchMapping("/{workflowId}/activate")
    public ResponseEntity<ApiResponse<Void>> activateWorkflow(
            @PathVariable String workflowId,
            @AuthenticationPrincipal String adminId) {

        workflowService.activateWorkflow(workflowId, adminId);
        return ResponseEntity.ok(ApiResponse.success(null, "Workflow activated"));
    }

    @PatchMapping("/{workflowId}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateWorkflow(
            @PathVariable String workflowId,
            @AuthenticationPrincipal String adminId) {

        workflowService.deactivateWorkflow(workflowId, adminId);
        return ResponseEntity.ok(ApiResponse.success(null, "Workflow deactivated"));
    }

    @DeleteMapping("/{workflowId}")
    public ResponseEntity<ApiResponse<Void>> deleteWorkflow(@PathVariable String workflowId) {
        workflowService.deleteWorkflow(workflowId);
        return ResponseEntity.ok(ApiResponse.success(null, "Workflow deleted"));
    }

    // ── Step endpoints ─────────────────────────────────────────────────────────

    @PostMapping("/{workflowId}/steps")
    public ResponseEntity<ApiResponse<ApprovalStep>> addStep(
            @PathVariable String workflowId,
            @Valid @RequestBody ApprovalStepRequest request,
            @AuthenticationPrincipal String adminId) {

        ApprovalStep step = mapToStep(request);
        ApprovalStep created = stepService.addStepToWorkflow(workflowId, step, adminId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Step added to workflow"));
    }

    @GetMapping("/{workflowId}/steps")
    public ResponseEntity<ApiResponse<List<ApprovalStep>>> getStepsForWorkflow(
            @PathVariable String workflowId) {
        return ResponseEntity.ok(ApiResponse.success(
                stepService.getStepsForWorkflow(workflowId)));
    }

    @PutMapping("/{workflowId}/steps/{stepId}")
    public ResponseEntity<ApiResponse<ApprovalStep>> updateStep(
            @PathVariable String workflowId,
            @PathVariable String stepId,
            @Valid @RequestBody ApprovalStepRequest request,
            @AuthenticationPrincipal String adminId) {

        ApprovalStep updated = stepService.updateStep(stepId, mapToStep(request), adminId);
        return ResponseEntity.ok(ApiResponse.success(updated, "Step updated"));
    }

    @PatchMapping("/{workflowId}/steps/reorder")
    public ResponseEntity<ApiResponse<Void>> reorderSteps(
            @PathVariable String workflowId,
            @Valid @RequestBody ReorderStepsRequest request) {

        stepService.reorderSteps(workflowId, request.getOrderedStepIds());
        return ResponseEntity.ok(ApiResponse.success(null, "Steps reordered"));
    }

    @PatchMapping("/{workflowId}/steps/{stepId}/activate")
    public ResponseEntity<ApiResponse<Void>> activateStep(
            @PathVariable String workflowId,
            @PathVariable String stepId) {

        stepService.activateStep(stepId);
        return ResponseEntity.ok(ApiResponse.success(null, "Step activated"));
    }

    @PatchMapping("/{workflowId}/steps/{stepId}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateStep(
            @PathVariable String workflowId,
            @PathVariable String stepId) {

        stepService.deactivateStep(stepId);
        return ResponseEntity.ok(ApiResponse.success(null, "Step deactivated"));
    }

    @DeleteMapping("/{workflowId}/steps/{stepId}")
    public ResponseEntity<ApiResponse<Void>> deleteStep(
            @PathVariable String workflowId,
            @PathVariable String stepId) {

        stepService.deleteStep(stepId);
        return ResponseEntity.ok(ApiResponse.success(null, "Step deleted"));
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private ApprovalWorkflow mapToWorkflow(ApprovalWorkflowRequest r) {
        ApprovalWorkflow w = new ApprovalWorkflow();
        w.setName(r.getName());
        w.setDescription(r.getDescription());
        w.setType(r.getType());
        w.setMinApprovers(r.getMinApprovers());
        w.setAmountThreshold(r.getAmountThreshold());
        w.setExpiryHours(r.getExpiryHours());
        return w;
    }

    private ApprovalStep mapToStep(ApprovalStepRequest r) {
        ApprovalStep s = new ApprovalStep();
        s.setName(r.getName());
        s.setDescription(r.getDescription());
        s.setRequiredRoleId(r.getRequiredRoleId());
        s.setAssignedTo(r.getAssignedTo());
        s.setRequiredApprovers(r.getRequiredApprovers());
        s.setBlocking(r.isBlocking());
        return s;
    }
}