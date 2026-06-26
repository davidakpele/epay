package com.example.admin_api_service.repository;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.admin_api_service.enums.ReconciliationReportStatus;
import com.example.admin_api_service.enums.ReconciliationReportType;
import com.example.admin_api_service.models.settlementsAndReconciliation.ReconciliationReport;

@Repository
public interface ReconciliationReportRepository extends JpaRepository<ReconciliationReport, String> {
    Optional<ReconciliationReport> findByReportReference(String reportReference);
    Page<ReconciliationReport> findAllByStatus(ReconciliationReportStatus status, Pageable pageable);
    Page<ReconciliationReport> findAllByReportType(ReconciliationReportType type, Pageable pageable);
    long countByReconciliationDate(LocalDate reconciliationDate);
}