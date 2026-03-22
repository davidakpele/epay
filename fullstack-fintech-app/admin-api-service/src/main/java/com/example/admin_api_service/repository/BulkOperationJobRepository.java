package com.example.admin_api_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.admin_api_service.enums.BulkOperationStatus;
import com.example.admin_api_service.enums.BulkOperationType;
import com.example.admin_api_service.models.accessAndApprovals.BulkOperationJob;

@Repository
public interface BulkOperationJobRepository extends JpaRepository<BulkOperationJob, String> {
    Page<BulkOperationJob> findAllByStatus(BulkOperationStatus status, Pageable pageable);
    Page<BulkOperationJob> findAllByOperationType(BulkOperationType type, Pageable pageable);
    Page<BulkOperationJob> findAllByCreatedBy(String createdBy, Pageable pageable);
}