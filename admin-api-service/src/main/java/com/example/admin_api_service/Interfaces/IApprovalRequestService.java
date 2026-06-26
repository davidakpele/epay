package com.example.admin_api_service.Interfaces;

import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.admin_api_service.enums.ApprovalRequestStatus;
import com.example.admin_api_service.enums.ApprovalTargetType;
import com.example.admin_api_service.enums.ApprovalWorkflowType;
import com.example.admin_api_service.models.accessAndApprovals.ApprovalRequest;

public interface IApprovalRequestService {
   // Create and route a new request through the resolved workflow
    ApprovalRequest submitRequest(ApprovalWorkflowType workflowType,
                                  ApprovalTargetType targetType,
                                  String targetId,
                                  BigDecimal amount,
                                  String currency,
                                  String payload,
                                  String requestedBy,
                                  String requestNote);
 
    ApprovalRequest getRequestById(String requestId);
 
    Page<ApprovalRequest> getAllRequests(Pageable pageable);
 
    Page<ApprovalRequest> getRequestsByStatus(ApprovalRequestStatus status, Pageable pageable);
 
    Page<ApprovalRequest> getRequestsByRequestedBy(String adminUserId, Pageable pageable);
 
    Page<ApprovalRequest> getPendingRequestsForAdmin(Long adminUserId, Pageable pageable);
 
    // Approve the current step — advance to next or mark fully approved
    ApprovalRequest approveStep(String requestId, String approvedBy, String note);
 
    // Reject the current step
    ApprovalRequest rejectRequest(String requestId, String rejectedBy, String note);
 
    ApprovalRequest cancelRequest(String requestId, String cancelledBy, String note);
 
    ApprovalRequest escalateRequest(String requestId, String escalatedBy, String note);
 
    void expireStaleRequests();
}
