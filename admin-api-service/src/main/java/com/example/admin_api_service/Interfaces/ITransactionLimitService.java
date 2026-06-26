package com.example.admin_api_service.Interfaces;

import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import com.example.admin_api_service.enums.TransactionLimitScope;
import com.example.admin_api_service.models.feeAndLimits.TransactionLimit;

public interface ITransactionLimitService {
    TransactionLimit createLimit(TransactionLimit limit, String createdBy);
 
    TransactionLimit updateLimit(String limitId, TransactionLimit updated, String updatedBy);
 
    TransactionLimit getLimitById(String limitId);
 
    TransactionLimit getLimitByCode(String code);
 
    Page<TransactionLimit> getAllLimits(Pageable pageable);
 
    List<TransactionLimit> getActiveLimits();
 
    List<TransactionLimit> getLimitsByScope(TransactionLimitScope scope);
 
    // Resolve the effective limit for a given transaction context
    Optional<TransactionLimit> resolveLimit(String transactionType, String channel,
                                            String currency, String kycTier);
 
    // Check if a single transaction amount exceeds any active limit
    boolean exceedsSingleTransactionLimit(Long userId, BigDecimal amount,
                                          String transactionType, String channel,
                                          String currency, String kycTier);
 
    void activateLimit(String limitId, String updatedBy);
 
    void deactivateLimit(String limitId, String updatedBy);
 
    // Supersede old limit and activate new one atomically
    TransactionLimit supersede(String oldLimitId, TransactionLimit newLimit, String updatedBy);
 
    void deleteLimit(String limitId);
}
