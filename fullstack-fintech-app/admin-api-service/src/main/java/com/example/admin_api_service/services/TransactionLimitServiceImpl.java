package com.example.admin_api_service.services;

import com.example.admin_api_service.Interfaces.ITransactionLimitOverrideService;
import com.example.admin_api_service.Interfaces.ITransactionLimitService;
import com.example.admin_api_service.enums.TransactionLimitScope;
import com.example.admin_api_service.enums.TransactionLimitStatus;
import com.example.admin_api_service.exceptions.ConflictException;
import com.example.admin_api_service.exceptions.ResourceNotFoundException;
import com.example.admin_api_service.models.feeAndLimits.TransactionLimit;
import com.example.admin_api_service.models.feeAndLimits.TransactionLimitOverride;
import com.example.admin_api_service.repository.TransactionLimitRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Service
@Transactional
public class TransactionLimitServiceImpl implements ITransactionLimitService {

    private final TransactionLimitRepository limitRepository;
    private final ITransactionLimitOverrideService overrideService;

    public TransactionLimitServiceImpl(TransactionLimitRepository limitRepository,
                                        @Lazy ITransactionLimitOverrideService overrideService) {
        this.limitRepository = limitRepository;
        this.overrideService = overrideService;
    }

    @Override
    public TransactionLimit createLimit(TransactionLimit limit, String createdBy) {
        if (limitRepository.existsByCode(limit.getCode())) {
            throw new ConflictException("Transaction limit with code '" + limit.getCode() + "' already exists");
        }
        limit.setCreatedBy(createdBy);
        limit.setStatus(TransactionLimitStatus.ACTIVE);
        if (limit.getEffectiveFrom() == null) {
            limit.setEffectiveFrom(LocalDateTime.now());
        }
        return limitRepository.save(limit);
    }

    @Override
    public TransactionLimit updateLimit(String limitId, TransactionLimit updated, String updatedBy) {
        TransactionLimit existing = getLimitById(limitId);

        if (!existing.getCode().equals(updated.getCode())
                && limitRepository.existsByCode(updated.getCode())) {
            throw new ConflictException("Transaction limit with code '" + updated.getCode() + "' already exists");
        }

        existing.setCode(updated.getCode());
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setLimitType(updated.getLimitType());
        existing.setScope(updated.getScope());
        existing.setTransactionType(updated.getTransactionType());
        existing.setChannel(updated.getChannel());
        existing.setKycTier(updated.getKycTier());
        existing.setCurrency(updated.getCurrency());
        existing.setSingleTransactionMax(updated.getSingleTransactionMax());
        existing.setSingleTransactionMin(updated.getSingleTransactionMin());
        existing.setDailyMax(updated.getDailyMax());
        existing.setDailyCountMax(updated.getDailyCountMax());
        existing.setWeeklyMax(updated.getWeeklyMax());
        existing.setWeeklyCountMax(updated.getWeeklyCountMax());
        existing.setMonthlyMax(updated.getMonthlyMax());
        existing.setMonthlyCountMax(updated.getMonthlyCountMax());
        existing.setEffectiveFrom(updated.getEffectiveFrom());
        existing.setEffectiveTo(updated.getEffectiveTo());
        existing.setUpdatedBy(updatedBy);
        existing.setUpdatedOn(LocalDateTime.now());
        return limitRepository.save(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionLimit getLimitById(String limitId) {
        return limitRepository.findById(limitId)
                .orElseThrow(() -> new ResourceNotFoundException("TransactionLimit", "id", limitId));
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionLimit getLimitByCode(String code) {
        return limitRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("TransactionLimit", "code", code));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionLimit> getAllLimits(Pageable pageable) {
        return limitRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionLimit> getActiveLimits() {
        return limitRepository.findAllByStatus(TransactionLimitStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionLimit> getLimitsByScope(TransactionLimitScope scope) {
        return limitRepository.findAllByScopeAndStatus(scope, TransactionLimitStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TransactionLimit> resolveLimit(String transactionType, String channel,
                                                   String currency, String kycTier) {
        LocalDateTime now = LocalDateTime.now();
        return limitRepository.findBestMatch(transactionType, channel, currency, kycTier, now);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exceedsSingleTransactionLimit(Long userId, BigDecimal amount,
                                                  String transactionType, String channel,
                                                  String currency, String kycTier) {
        // Check user-level override first
        Optional<TransactionLimitOverride> override = overrideService.getActiveOverrideForUser(userId, null);
        if (override.isPresent()) {
            TransactionLimitOverride ov = override.get();
            if (ov.getSingleTransactionMax() != null) {
                return amount.compareTo(ov.getSingleTransactionMax()) > 0;
            }
        }

        // Fall back to base limit
        return resolveLimit(transactionType, channel, currency, kycTier)
                .map(limit -> limit.getSingleTransactionMax() != null
                        && amount.compareTo(limit.getSingleTransactionMax()) > 0)
                .orElse(false);
    }

    @Override
    public void activateLimit(String limitId, String updatedBy) {
        TransactionLimit limit = getLimitById(limitId);
        limit.setStatus(TransactionLimitStatus.ACTIVE);
        limit.setUpdatedBy(updatedBy);
        limit.setUpdatedOn(LocalDateTime.now());
        limitRepository.save(limit);
    }

    @Override
    public void deactivateLimit(String limitId, String updatedBy) {
        TransactionLimit limit = getLimitById(limitId);
        limit.setStatus(TransactionLimitStatus.INACTIVE);
        limit.setUpdatedBy(updatedBy);
        limit.setUpdatedOn(LocalDateTime.now());
        limitRepository.save(limit);
    }

    @Override
    public TransactionLimit supersede(String oldLimitId, TransactionLimit newLimit, String updatedBy) {
        TransactionLimit old = getLimitById(oldLimitId);
        old.setStatus(TransactionLimitStatus.SUPERSEDED);
        old.setEffectiveTo(LocalDateTime.now());
        old.setUpdatedBy(updatedBy);
        old.setUpdatedOn(LocalDateTime.now());
        limitRepository.save(old);

        newLimit.setCreatedBy(updatedBy);
        newLimit.setStatus(TransactionLimitStatus.ACTIVE);
        newLimit.setEffectiveFrom(LocalDateTime.now());
        return limitRepository.save(newLimit);
    }

    @Override
    public void deleteLimit(String limitId) {
        TransactionLimit limit = getLimitById(limitId);
        if (limit.getStatus() == TransactionLimitStatus.ACTIVE) {
            throw new ConflictException("Active transaction limits cannot be deleted. Deactivate first.");
        }
        limitRepository.delete(limit);
    }
}