package com.example.admin_api_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.admin_api_service.enums.SanctionScreeningResult;
import com.example.admin_api_service.models.complianceAndRisk.SanctionScreeningLog;

@Repository
public interface SanctionScreeningLogRepository extends JpaRepository<SanctionScreeningLog, String> {
    Page<SanctionScreeningLog> findAllByUserId(Long userId, Pageable pageable);
    Page<SanctionScreeningLog> findAllByResult(SanctionScreeningResult result, Pageable pageable);
    Page<SanctionScreeningLog> findAllByTransactionId(String transactionId, Pageable pageable);
}