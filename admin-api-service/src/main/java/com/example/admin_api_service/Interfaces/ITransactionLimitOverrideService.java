package com.example.admin_api_service.Interfaces;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.admin_api_service.enums.LimitOverrideStatus;
import com.example.admin_api_service.models.feeAndLimits.TransactionLimitOverride;

public interface ITransactionLimitOverrideService {
    TransactionLimitOverride applyOverride(String transactionLimitId, Long userId, Long walletId,
                                           String reason, String internalNote,
                                           BigDecimal singleTransactionMax,
                                           BigDecimal singleTransactionMin,
                                           BigDecimal dailyMax, Integer dailyCountMax,
                                           BigDecimal weeklyMax, Integer weeklyCountMax,
                                           BigDecimal monthlyMax, Integer monthlyCountMax,
                                           String appliedBy, String ipAddress,
                                           LocalDateTime expiresAt);
 
    TransactionLimitOverride getOverrideById(String overrideId);
 
    Page<TransactionLimitOverride> getAllOverrides(Pageable pageable);
 
    Page<TransactionLimitOverride> getOverridesByStatus(LimitOverrideStatus status, Pageable pageable);
 
    List<TransactionLimitOverride> getOverridesByUser(Long userId);
 
    Optional<TransactionLimitOverride> getActiveOverrideForUser(Long userId, Long walletId);
 
    TransactionLimitOverride approveOverride(String overrideId, String approvedBy);
 
    TransactionLimitOverride revokeOverride(String overrideId, String revokedBy, String revokeReason);
 
    void expireStaleOverrides();
 
    // Resolve effective limit value merging override (non-null fields) over the base limit
    BigDecimal resolveEffectiveSingleMax(Long userId, Long walletId, BigDecimal baseLimitMax);
 
    BigDecimal resolveEffectiveDailyMax(Long userId, Long walletId, BigDecimal baseDailyMax);
 
    BigDecimal resolveEffectiveMonthlyMax(Long userId, Long walletId, BigDecimal baseMonthlyMax);
}
