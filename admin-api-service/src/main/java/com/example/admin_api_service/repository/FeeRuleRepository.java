package com.example.admin_api_service.repository;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.admin_api_service.models.feeAndLimits.FeeRule;

@Repository
public interface FeeRuleRepository extends JpaRepository<FeeRule, String> {
    List<FeeRule> findAllByFeeConfigurationIdOrderByMinAmountAsc(String feeConfigurationId);
 
    // Match the band where minAmount <= amount <= maxAmount (null maxAmount = no upper cap)
    @Query("SELECT r FROM FeeRule r " +
           "WHERE r.feeConfigurationId = :configId " +
           "AND r.minAmount <= :amount " +
           "AND (r.maxAmount IS NULL OR r.maxAmount >= :amount) " +
           "ORDER BY r.minAmount DESC")
    Optional<FeeRule> findMatchingRule(@Param("configId") String configId,
                                       @Param("amount") BigDecimal amount);
 
    @Modifying
    @Query("DELETE FROM FeeRule r WHERE r.feeConfigurationId = :configId")
    void deleteAllByFeeConfigurationId(@Param("configId") String configId);
}