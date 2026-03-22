package com.example.admin_api_service.Interfaces;

import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.admin_api_service.enums.AdjustmentType;
import com.example.admin_api_service.enums.ManualAdjustmentStatus;
import com.example.admin_api_service.models.accessAndApprovals.ManualAdjustmentRequest;

public interface IManualAdjustmentRequestService {
ManualAdjustmentRequest submitAdjustment(Long walletId, Long userId,
                                             AdjustmentType adjustmentType,
                                             BigDecimal amount, String currency,
                                             BigDecimal balanceBefore,
                                             String reason, String internalNote,
                                             String requestedBy, String ipAddress);
 
    ManualAdjustmentRequest getAdjustmentById(String adjustmentId);
 
    Page<ManualAdjustmentRequest> getAllAdjustments(Pageable pageable);
 
    Page<ManualAdjustmentRequest> getAdjustmentsByStatus(ManualAdjustmentStatus status, Pageable pageable);
 
    Page<ManualAdjustmentRequest> getAdjustmentsByWallet(Long walletId, Pageable pageable);
 
    Page<ManualAdjustmentRequest> getAdjustmentsByUser(Long userId, Pageable pageable);
 
    ManualAdjustmentRequest approveAdjustment(String adjustmentId, String approvedBy);
 
    ManualAdjustmentRequest rejectAdjustment(String adjustmentId, String rejectedBy, String rejectionReason);
 
    // Called after funds are actually moved — sets EXECUTED + balanceAfter
    ManualAdjustmentRequest markExecuted(String adjustmentId, BigDecimal balanceAfter,
                                         String historyReferenceId);
 
    ManualAdjustmentRequest markFailed(String adjustmentId, String failureReason);
 
    ManualAdjustmentRequest cancelAdjustment(String adjustmentId, String cancelledBy);
}
