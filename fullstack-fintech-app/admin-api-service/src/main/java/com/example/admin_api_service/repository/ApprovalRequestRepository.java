package com.example.admin_api_service.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.admin_api_service.enums.ApprovalRequestStatus;
import com.example.admin_api_service.models.accessAndApprovals.ApprovalRequest;

@Repository
public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, String> {
    Page<ApprovalRequest> findAllByStatus(ApprovalRequestStatus status, Pageable pageable);
    Page<ApprovalRequest> findAllByRequestedBy(String requestedBy, Pageable pageable);
    List<ApprovalRequest> findAllByStatusInAndExpiresAtBefore(List<ApprovalRequestStatus> statuses, LocalDateTime now);
 
    // Pending requests where the current step requires a specific admin's role
        @Query("""
    SELECT ar FROM ApprovalRequest ar
    JOIN ar.currentStep s
    JOIN s.requiredRole r
    JOIN AdminUser u
    WHERE u.role = r
    AND u.id = :adminUserId
    AND ar.status IN (com.example.admin_api_service.enums.ApprovalRequestStatus.PENDING,
                      com.example.admin_api_service.enums.ApprovalRequestStatus.IN_REVIEW)""")
        Page<ApprovalRequest> findPendingRequestsForAdmin(
        @Param("adminUserId") Long adminUserId,
        Pageable pageable);
}
