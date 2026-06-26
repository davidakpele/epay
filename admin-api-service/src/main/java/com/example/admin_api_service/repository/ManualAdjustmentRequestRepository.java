package com.example.admin_api_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.admin_api_service.enums.ManualAdjustmentStatus;
import com.example.admin_api_service.models.accessAndApprovals.ManualAdjustmentRequest;

@Repository
public interface ManualAdjustmentRequestRepository extends JpaRepository<ManualAdjustmentRequest, String> {
    Page<ManualAdjustmentRequest> findAllByStatus(ManualAdjustmentStatus status, Pageable pageable);
    Page<ManualAdjustmentRequest> findAllByWalletId(Long walletId, Pageable pageable);
    Page<ManualAdjustmentRequest> findAllByUserId(Long userId, Pageable pageable);
}
