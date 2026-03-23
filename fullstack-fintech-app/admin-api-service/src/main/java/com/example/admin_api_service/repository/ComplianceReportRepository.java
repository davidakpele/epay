package com.example.admin_api_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.admin_api_service.enums.ComplianceReportStatus;
import com.example.admin_api_service.enums.ComplianceReportType;
import com.example.admin_api_service.models.complianceAndRisk.ComplianceReport;

@Repository
public interface ComplianceReportRepository extends JpaRepository<ComplianceReport, String> {
    Optional<ComplianceReport> findByReportReference(String reportReference);
    Page<ComplianceReport> findAllByStatus(ComplianceReportStatus status, Pageable pageable);
    Page<ComplianceReport> findAllByReportType(ComplianceReportType reportType, Pageable pageable);
}
