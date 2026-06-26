package com.example.admin_api_service.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.admin_api_service.enums.ReconciliationExceptionStatus;
import com.example.admin_api_service.models.settlementsAndReconciliation.ReconciliationException;

@Repository
public interface ReconciliationExceptionRepository extends JpaRepository<ReconciliationException, String> {
    Page<ReconciliationException> findAllByReconciliationReportId(String reportId, Pageable pageable);
    Page<ReconciliationException> findAllByStatus(ReconciliationExceptionStatus status, Pageable pageable);
    List<ReconciliationException> findAllByReconciliationReportIdAndStatusIn(
    String reportId, List<ReconciliationExceptionStatus> statuses);
}