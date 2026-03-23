package com.example.admin_api_service.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import com.example.admin_api_service.enums.FeeConfigurationStatus;
import com.example.admin_api_service.enums.FeeConfigurationType;
import com.example.admin_api_service.models.feeAndLimits.FeeConfiguration;

@Repository
public interface FeeConfigurationRepository extends JpaRepository<FeeConfiguration, String> {
    Optional<FeeConfiguration> findByCode(String code);
    boolean existsByCode(String code);
    List<FeeConfiguration> findAllByStatus(FeeConfigurationStatus status);
    List<FeeConfiguration> findAllByTypeAndStatus(FeeConfigurationType type, FeeConfigurationStatus status);
 
    @Query("SELECT f FROM FeeConfiguration f " +
           "WHERE f.type = :type " +
           "AND f.status = 'ACTIVE' " +
           "AND f.currency = :currency " +
           "AND (f.channel = :channel OR f.channel IS NULL) " +
           "AND (f.kycTier = :kycTier OR f.kycTier IS NULL) " +
           "AND f.effectiveFrom <= :now " +
           "AND (f.effectiveTo IS NULL OR f.effectiveTo > :now) " +
           "ORDER BY " +
           "  CASE WHEN f.channel IS NOT NULL THEN 1 ELSE 0 END DESC, " +
           "  CASE WHEN f.kycTier IS NOT NULL THEN 1 ELSE 0 END DESC")
    Optional<FeeConfiguration> findBestMatch(@Param("type") FeeConfigurationType type,
                                              @Param("channel") String channel,
                                              @Param("currency") String currency,
                                              @Param("kycTier") String kycTier,
                                              @Param("now") LocalDateTime now);
}