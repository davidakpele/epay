package com.example.admin_api_service.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.admin_api_service.models.systemAndConfiguration.ExchangeRateSnapshot;


@Repository
public interface ExchangeRateSnapshotRepository extends JpaRepository<ExchangeRateSnapshot, String> {
    Page<ExchangeRateSnapshot> findAllByBaseCurrencyAndTargetCurrencyOrderByEffectiveAtDesc(
            String baseCurrency, String targetCurrency, Pageable pageable);
 
    @Query("SELECT r FROM ExchangeRateSnapshot r " +
           "WHERE r.baseCurrency = :base AND r.targetCurrency = :target " +
           "AND r.isActive = true " +
           "AND r.effectiveAt <= :now " +
           "AND (r.expiresAt IS NULL OR r.expiresAt > :now) " +
           "ORDER BY r.effectiveAt DESC")
    Optional<ExchangeRateSnapshot> findActiveRate(@Param("base") String base,
                                                   @Param("target") String target,
                                                   @Param("now") LocalDateTime now);
 
    List<ExchangeRateSnapshot> findAllByIsActiveTrueAndEffectiveAtBeforeAndExpiresAtAfterOrExpiresAtIsNull(
            LocalDateTime now1, LocalDateTime now2);
}