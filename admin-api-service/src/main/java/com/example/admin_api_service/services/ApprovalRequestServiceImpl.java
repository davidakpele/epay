package com.example.admin_api_service.services;


import com.example.admin_api_service.Interfaces.IApprovalRequestService;
import com.example.admin_api_service.Interfaces.IApprovalStepService;
import com.example.admin_api_service.Interfaces.IApprovalWorkflowService;
import com.example.admin_api_service.enums.ApprovalRequestStatus;
import com.example.admin_api_service.enums.ApprovalTargetType;
import com.example.admin_api_service.enums.ApprovalWorkflowType;
import com.example.admin_api_service.exceptions.BadRequestException;
import com.example.admin_api_service.exceptions.ConflictException;
import com.example.admin_api_service.exceptions.ResourceNotFoundException;
import com.example.admin_api_service.models.accessAndApprovals.ApprovalRequest;
import com.example.admin_api_service.models.accessAndApprovals.ApprovalStep;
import com.example.admin_api_service.models.accessAndApprovals.ApprovalWorkflow;
import com.example.admin_api_service.repository.ApprovalRequestRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ApprovalRequestServiceImpl implements IApprovalRequestService {

    private final ApprovalRequestRepository requestRepository;
    private final IApprovalWorkflowService workflowService;
    private final IApprovalStepService stepService;

    public ApprovalRequestServiceImpl(ApprovalRequestRepository requestRepository,
                                      IApprovalWorkflowService workflowService,
                                      IApprovalStepService stepService) {
        this.requestRepository = requestRepository;
        this.workflowService = workflowService;
        this.stepService = stepService;
    }

    @Override
    public ApprovalRequest submitRequest(ApprovalWorkflowType workflowType,
                                         ApprovalTargetType targetType,
                                         String targetId,
                                         BigDecimal amount,
                                         String currency,
                                         String payload,
                                         String requestedBy,
                                         String requestNote) {

        ApprovalWorkflow workflow = workflowService.resolveWorkflow(workflowType, amount);
        ApprovalStep firstStep = stepService.getFirstStep(workflow.getId());

        ApprovalRequest request = new ApprovalRequest();
        request.setWorkflowId(workflow.getId());
        request.setCurrentStepId(firstStep.getId());
        request.setTargetType(targetType);
        request.setTargetId(targetId);
        request.setAmount(amount);
        request.setCurrency(currency);
        request.setPayload(payload);
        request.setRequestedBy(requestedBy);
        request.setRequestNote(requestNote);
        request.setStatus(ApprovalRequestStatus.PENDING);

        if (workflow.getExpiryHours() != null) {
            request.setExpiresAt(LocalDateTime.now().plusHours(workflow.getExpiryHours()));
        }

        return requestRepository.save(request);
    }

    @Override
    @Transactional(readOnly = true)
    public ApprovalRequest getRequestById(String requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("ApprovalRequest", "id", requestId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ApprovalRequest> getAllRequests(Pageable pageable) {
        return requestRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ApprovalRequest> getRequestsByStatus(ApprovalRequestStatus status, Pageable pageable) {
        return requestRepository.findAllByStatus(status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ApprovalRequest> getRequestsByRequestedBy(String adminUserId, Pageable pageable) {
        return requestRepository.findAllByRequestedBy(adminUserId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ApprovalRequest> getPendingRequestsForAdmin(Long adminUserId, Pageable pageable) {
        return requestRepository.findPendingRequestsForAdmin(adminUserId, pageable);
    }

    @Override
    public ApprovalRequest approveStep(String requestId, String approvedBy, String note) {
        ApprovalRequest request = getRequestById(requestId);
        validateRequestIsActionable(request);

        ApprovalStep currentStep = stepService.getStepById(request.getCurrentStepId());

        // Advance to next step or mark fully approved
        ApprovalStep nextStep = stepService.getNextStep(
                request.getWorkflowId(), currentStep.getStepOrder());

        if (nextStep == null) {
            // All steps completed — fully approved
            request.setStatus(ApprovalRequestStatus.APPROVED);
            request.setResolvedBy(approvedBy);
            request.setResolvedNote(note);
            request.setResolvedAt(LocalDateTime.now());
            request.setCurrentStepId(null);
        } else {
            // Move to next step
            request.setStatus(ApprovalRequestStatus.IN_REVIEW);
            request.setCurrentStepId(nextStep.getId());
        }

        request.setUpdatedOn(LocalDateTime.now());
        return requestRepository.save(request);
    }

    @Override
    public ApprovalRequest rejectRequest(String requestId, String rejectedBy, String note) {
        ApprovalRequest request = getRequestById(requestId);
        validateRequestIsActionable(request);

        request.setStatus(ApprovalRequestStatus.REJECTED);
        request.setResolvedBy(rejectedBy);
        request.setResolvedNote(note);
        request.setResolvedAt(LocalDateTime.now());
        request.setUpdatedOn(LocalDateTime.now());
        return requestRepository.save(request);
    }

    @Override
    public ApprovalRequest cancelRequest(String requestId, String cancelledBy, String note) {
        ApprovalRequest request = getRequestById(requestId);
        if (request.getStatus() == ApprovalRequestStatus.APPROVED
                || request.getStatus() == ApprovalRequestStatus.REJECTED) {
            throw new ConflictException("Cannot cancel a request that is already " + request.getStatus());
        }
        request.setStatus(ApprovalRequestStatus.CANCELLED);
        request.setResolvedBy(cancelledBy);
        request.setResolvedNote(note);
        request.setResolvedAt(LocalDateTime.now());
        request.setUpdatedOn(LocalDateTime.now());
        return requestRepository.save(request);
    }

    @Override
    public ApprovalRequest escalateRequest(String requestId, String escalatedBy, String note) {
        ApprovalRequest request = getRequestById(requestId);
        validateRequestIsActionable(request);
        request.setStatus(ApprovalRequestStatus.ESCALATED);
        request.setResolvedNote(note);
        request.setUpdatedOn(LocalDateTime.now());
        return requestRepository.save(request);
    }

    @Override
    @Scheduled(fixedDelay = 600000) // runs every 10 minutes
    public void expireStaleRequests() {
        List<ApprovalRequest> stale = requestRepository
                .findAllByStatusInAndExpiresAtBefore(
                        List.of(ApprovalRequestStatus.PENDING, ApprovalRequestStatus.IN_REVIEW),
                        LocalDateTime.now());
        stale.forEach(r -> {
            r.setStatus(ApprovalRequestStatus.EXPIRED);
            r.setResolvedAt(LocalDateTime.now());
            r.setUpdatedOn(LocalDateTime.now());
        });
        if (!stale.isEmpty()) {
            requestRepository.saveAll(stale);
        }
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private void validateRequestIsActionable(ApprovalRequest request) {
        if (request.getStatus() == ApprovalRequestStatus.APPROVED
                || request.getStatus() == ApprovalRequestStatus.REJECTED
                || request.getStatus() == ApprovalRequestStatus.CANCELLED
                || request.getStatus() == ApprovalRequestStatus.EXPIRED) {
            throw new ConflictException("Request is already in a terminal state: " + request.getStatus());
        }
        if (request.getExpiresAt() != null
                && request.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("This approval request has expired");
        }
    }
}