package com.example.admin_api_service.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.admin_api_service.enums.TransactionLimitScope;
import com.example.admin_api_service.enums.TransactionLimitStatus;
import com.example.admin_api_service.models.feeAndLimits.TransactionLimit;

@Repository
public interface TransactionLimitRepository extends JpaRepository<TransactionLimit, String> {
    Optional<TransactionLimit> findByCode(String code);
    boolean existsByCode(String code);
    List<TransactionLimit> findAllByStatus(TransactionLimitStatus status);
    List<TransactionLimit> findAllByScopeAndStatus(TransactionLimitScope scope, TransactionLimitStatus status);
 
    @Query("SELECT l FROM TransactionLimit l " +
           "WHERE l.status = 'ACTIVE' " +
           "AND l.currency = :currency " +
           "AND (l.transactionType = :transactionType OR l.transactionType IS NULL) " +
           "AND (l.channel = :channel OR l.channel IS NULL) " +
           "AND (l.kycTier = :kycTier OR l.kycTier IS NULL) " +
           "AND l.effectiveFrom <= :now " +
           "AND (l.effectiveTo IS NULL OR l.effectiveTo > :now) " +
           "ORDER BY " +
           "  CASE WHEN l.kycTier IS NOT NULL THEN 1 ELSE 0 END DESC, " +
           "  CASE WHEN l.transactionType IS NOT NULL THEN 1 ELSE 0 END DESC, " +
           "  CASE WHEN l.channel IS NOT NULL THEN 1 ELSE 0 END DESC")
    Optional<TransactionLimit> findBestMatch(@Param("transactionType") String transactionType, @Param("channel") String channel, @Param("currency") String currency, @Param("kycTier") String kycTier, @Param("now") LocalDateTime now);
}
