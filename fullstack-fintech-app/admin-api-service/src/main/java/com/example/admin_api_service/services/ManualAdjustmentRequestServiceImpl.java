package com.example.admin_api_service.services;

import com.example.admin_api_service.Interfaces.IApprovalRequestService;
import com.example.admin_api_service.Interfaces.IManualAdjustmentRequestService;
import com.example.admin_api_service.enums.AdjustmentType;
import com.example.admin_api_service.enums.ApprovalTargetType;
import com.example.admin_api_service.enums.ApprovalWorkflowType;
import com.example.admin_api_service.enums.ManualAdjustmentStatus;
import com.example.admin_api_service.exceptions.ConflictException;
import com.example.admin_api_service.exceptions.ResourceNotFoundException;
import com.example.admin_api_service.models.accessAndApprovals.ApprovalRequest;
import com.example.admin_api_service.models.accessAndApprovals.ManualAdjustmentRequest;
import com.example.admin_api_service.repository.ManualAdjustmentRequestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Transactional
public class ManualAdjustmentRequestServiceImpl implements IManualAdjustmentRequestService {

    private final ManualAdjustmentRequestRepository adjustmentRepository;
    private final IApprovalRequestService approvalRequestService;

    public ManualAdjustmentRequestServiceImpl(ManualAdjustmentRequestRepository adjustmentRepository,
                                              IApprovalRequestService approvalRequestService) {
        this.adjustmentRepository = adjustmentRepository;
        this.approvalRequestService = approvalRequestService;
    }

    @Override
    public ManualAdjustmentRequest submitAdjustment(Long walletId, Long userId,
                                                    AdjustmentType adjustmentType,
                                                    BigDecimal amount, String currency,
                                                    BigDecimal balanceBefore,
                                                    String reason, String internalNote,
                                                    String requestedBy, String ipAddress) {
        ManualAdjustmentRequest adjustment = new ManualAdjustmentRequest();
        adjustment.setWalletId(walletId);
        adjustment.setUserId(userId);
        adjustment.setAdjustmentType(adjustmentType);
        adjustment.setAmount(amount);
        adjustment.setCurrency(currency);
        adjustment.setBalanceBefore(balanceBefore);
        adjustment.setReason(reason);
        adjustment.setInternalNote(internalNote);
        adjustment.setRequestedBy(requestedBy);
        adjustment.setIpAddress(ipAddress);
        adjustment.setStatus(ManualAdjustmentStatus.PENDING);

        ManualAdjustmentRequest saved = adjustmentRepository.save(adjustment);

        // Submit through approval workflow
        try {
            ApprovalRequest approvalRequest = approvalRequestService.submitRequest(
                    ApprovalWorkflowType.MANUAL_ADJUSTMENT,
                    ApprovalTargetType.WALLET,
                    String.valueOf(walletId),
                    amount,
                    currency,
                    reason,
                    requestedBy,
                    reason
            );
            saved.setApprovalRequestId(approvalRequest.getId());
            saved.setStatus(ManualAdjustmentStatus.AWAITING_APPROVAL);
        } catch (ResourceNotFoundException e) {
            // No workflow configured — auto-approve if no workflow required
            saved.setStatus(ManualAdjustmentStatus.APPROVED);
            saved.setApprovedBy(requestedBy);
            saved.setApprovedAt(LocalDateTime.now());
        }

        return adjustmentRepository.save(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ManualAdjustmentRequest getAdjustmentById(String adjustmentId) {
        return adjustmentRepository.findById(adjustmentId)
                .orElseThrow(() -> new ResourceNotFoundException("ManualAdjustmentRequest", "id", adjustmentId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ManualAdjustmentRequest> getAllAdjustments(Pageable pageable) {
        return adjustmentRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ManualAdjustmentRequest> getAdjustmentsByStatus(ManualAdjustmentStatus status, Pageable pageable) {
        return adjustmentRepository.findAllByStatus(status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ManualAdjustmentRequest> getAdjustmentsByWallet(Long walletId, Pageable pageable) {
        return adjustmentRepository.findAllByWalletId(walletId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ManualAdjustmentRequest> getAdjustmentsByUser(Long userId, Pageable pageable) {
        return adjustmentRepository.findAllByUserId(userId, pageable);
    }

    @Override
    public ManualAdjustmentRequest approveAdjustment(String adjustmentId, String approvedBy) {
        ManualAdjustmentRequest adjustment = getAdjustmentById(adjustmentId);
        if (adjustment.getStatus() != ManualAdjustmentStatus.AWAITING_APPROVAL
                && adjustment.getStatus() != ManualAdjustmentStatus.PENDING) {
            throw new ConflictException("Adjustment cannot be approved in its current state: " + adjustment.getStatus());
        }
        adjustment.setStatus(ManualAdjustmentStatus.APPROVED);
        adjustment.setApprovedBy(approvedBy);
        adjustment.setApprovedAt(LocalDateTime.now());
        adjustment.setUpdatedOn(LocalDateTime.now());
        return adjustmentRepository.save(adjustment);
    }

    @Override
    public ManualAdjustmentRequest rejectAdjustment(String adjustmentId, String rejectedBy, String rejectionReason) {
        ManualAdjustmentRequest adjustment = getAdjustmentById(adjustmentId);
        if (adjustment.getStatus() != ManualAdjustmentStatus.AWAITING_APPROVAL
                && adjustment.getStatus() != ManualAdjustmentStatus.PENDING) {
            throw new ConflictException("Adjustment cannot be rejected in its current state: " + adjustment.getStatus());
        }
        adjustment.setStatus(ManualAdjustmentStatus.REJECTED);
        adjustment.setRejectedBy(rejectedBy);
        adjustment.setRejectionReason(rejectionReason);
        adjustment.setRejectedAt(LocalDateTime.now());
        adjustment.setUpdatedOn(LocalDateTime.now());
        return adjustmentRepository.save(adjustment);
    }

    @Override
    public ManualAdjustmentRequest markExecuted(String adjustmentId, BigDecimal balanceAfter,
                                                String historyReferenceId) {
        ManualAdjustmentRequest adjustment = getAdjustmentById(adjustmentId);
        if (adjustment.getStatus() != ManualAdjustmentStatus.APPROVED) {
            throw new ConflictException("Only approved adjustments can be executed");
        }
        adjustment.setStatus(ManualAdjustmentStatus.EXECUTED);
        adjustment.setBalanceAfter(balanceAfter);
        adjustment.setHistoryReferenceId(historyReferenceId);
        adjustment.setExecutedAt(LocalDateTime.now());
        adjustment.setUpdatedOn(LocalDateTime.now());
        return adjustmentRepository.save(adjustment);
    }

    @Override
    public ManualAdjustmentRequest markFailed(String adjustmentId, String failureReason) {
        ManualAdjustmentRequest adjustment = getAdjustmentById(adjustmentId);
        adjustment.setStatus(ManualAdjustmentStatus.FAILED);
        adjustment.setRejectionReason(failureReason);
        adjustment.setUpdatedOn(LocalDateTime.now());
        return adjustmentRepository.save(adjustment);
    }

    @Override
    public ManualAdjustmentRequest cancelAdjustment(String adjustmentId, String cancelledBy) {
        ManualAdjustmentRequest adjustment = getAdjustmentById(adjustmentId);
        if (adjustment.getStatus() == ManualAdjustmentStatus.EXECUTED
                || adjustment.getStatus() == ManualAdjustmentStatus.FAILED) {
            throw new ConflictException("Cannot cancel an adjustment that is already " + adjustment.getStatus());
        }
        adjustment.setStatus(ManualAdjustmentStatus.CANCELLED);
        adjustment.setUpdatedOn(LocalDateTime.now());
        return adjustmentRepository.save(adjustment);
    }
}