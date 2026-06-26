package com.example.admin_api_service.repository;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.admin_api_service.enums.SettlementBatchStatus;
import com.example.admin_api_service.enums.SettlementBatchType;
import com.example.admin_api_service.models.settlementsAndReconciliation.SettlementBatch;


@Repository
public interface SettlementBatchRepository extends JpaRepository<SettlementBatch, String> {
    Optional<SettlementBatch> findByBatchReference(String batchReference);
    Page<SettlementBatch> findAllByStatus(SettlementBatchStatus status, Pageable pageable);
    Page<SettlementBatch> findAllByBatchType(SettlementBatchType type, Pageable pageable);
    Page<SettlementBatch> findAllBySettlementDate(LocalDate settlementDate, Pageable pageable);
    long countBySettlementDate(LocalDate settlementDate);
}
