package com.example.admin_api_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.admin_api_service.enums.ApprovalWorkflowType;
import com.example.admin_api_service.models.accessAndApprovals.ApprovalWorkflow;

@Repository
public interface ApprovalWorkflowRepository extends JpaRepository<ApprovalWorkflow, String> {
    Optional<ApprovalWorkflow> findByName(String name);
    boolean existsByName(String name);
    List<ApprovalWorkflow> findAllByIsActiveTrue();
    List<ApprovalWorkflow> findAllByTypeAndIsActiveTrue(ApprovalWorkflowType type);
}