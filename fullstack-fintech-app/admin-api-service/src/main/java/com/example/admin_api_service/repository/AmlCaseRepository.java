package com.example.admin_api_service.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.admin_api_service.enums.AmlCasePriority;
import com.example.admin_api_service.enums.AmlCaseStatus;
import com.example.admin_api_service.models.complianceAndRisk.AmlCase;

@Repository
public interface AmlCaseRepository extends JpaRepository<AmlCase, String> {
    Optional<AmlCase> findByCaseReference(String caseReference);
    Page<AmlCase> findAllByStatus(AmlCaseStatus status, Pageable pageable);
    Page<AmlCase> findAllByPriority(AmlCasePriority priority, Pageable pageable);
    Page<AmlCase> findAllByUserId(Long userId, Pageable pageable);
}