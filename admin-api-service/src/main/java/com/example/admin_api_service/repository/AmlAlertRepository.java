package com.example.admin_api_service.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.admin_api_service.enums.AmlAlertSeverity;
import com.example.admin_api_service.enums.AmlAlertStatus;
import com.example.admin_api_service.models.complianceAndRisk.AmlAlert;

@Repository
public interface AmlAlertRepository extends JpaRepository<AmlAlert, String> {
    Page<AmlAlert> findAllByStatus(AmlAlertStatus status, Pageable pageable);
    Page<AmlAlert> findAllBySeverity(AmlAlertSeverity severity, Pageable pageable);
    Page<AmlAlert> findAllByUserId(Long userId, Pageable pageable);
    List<AmlAlert> findAllByUserIdAndStatus(Long userId, AmlAlertStatus status);
}