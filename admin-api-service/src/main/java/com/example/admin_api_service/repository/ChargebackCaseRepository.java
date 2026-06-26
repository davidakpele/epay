package com.example.admin_api_service.repository;


import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.admin_api_service.enums.ChargebackCaseStatus;
import com.example.admin_api_service.models.settlementsAndReconciliation.ChargebackCase;

@Repository
public interface ChargebackCaseRepository extends JpaRepository<ChargebackCase, String> {
    Optional<ChargebackCase> findByCaseReference(String caseReference);
    Page<ChargebackCase> findAllByStatus(ChargebackCaseStatus status, Pageable pageable);
    Page<ChargebackCase> findAllByUserId(Long userId, Pageable pageable);
    Page<ChargebackCase> findAllByWalletId(Long walletId, Pageable pageable);
}