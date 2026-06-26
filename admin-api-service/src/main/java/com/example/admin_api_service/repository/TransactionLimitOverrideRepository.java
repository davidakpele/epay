package com.example.admin_api_service.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.admin_api_service.enums.LimitOverrideStatus;
import com.example.admin_api_service.models.feeAndLimits.TransactionLimitOverride;

@Repository
public interface TransactionLimitOverrideRepository extends JpaRepository<TransactionLimitOverride, String> {
    Page<TransactionLimitOverride> findAllByStatus(LimitOverrideStatus status, Pageable pageable);
    List<TransactionLimitOverride> findAllByUserId(Long userId);
    Optional<TransactionLimitOverride> findByUserIdAndWalletIdAndStatus(Long userId, Long walletId, LimitOverrideStatus status);
    Optional<TransactionLimitOverride> findTopByUserIdAndStatusOrderByAppliedAtDesc(Long userId, LimitOverrideStatus status);
    boolean existsByTransactionLimitIdAndUserIdAndStatus(String transactionLimitId, Long userId, LimitOverrideStatus status);
    List<TransactionLimitOverride> findAllByStatusAndExpiresAtBefore(LimitOverrideStatus status, LocalDateTime now);
}