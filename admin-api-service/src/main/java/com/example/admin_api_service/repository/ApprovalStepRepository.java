package com.example.admin_api_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.admin_api_service.enums.ApprovalStepStatus;
import com.example.admin_api_service.models.accessAndApprovals.ApprovalStep;

@Repository
public interface ApprovalStepRepository extends JpaRepository<ApprovalStep, String> {
    List<ApprovalStep> findAllByWorkflowIdOrderByStepOrderAsc(String workflowId);
    int countByWorkflowId(String workflowId);
    Optional<ApprovalStep> findFirstByWorkflowIdAndStatusOrderByStepOrderAsc(
            String workflowId, ApprovalStepStatus status);
    Optional<ApprovalStep> findFirstByWorkflowIdAndStepOrderGreaterThanAndStatusOrderByStepOrderAsc(String workflowId, int stepOrder, ApprovalStepStatus status);

}
