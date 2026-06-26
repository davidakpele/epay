package com.example.admin_api_service.services;

import com.example.admin_api_service.Interfaces.ITransactionLimitOverrideService;
import com.example.admin_api_service.Interfaces.ITransactionLimitService;
import com.example.admin_api_service.enums.LimitOverrideStatus;
import com.example.admin_api_service.exceptions.ConflictException;
import com.example.admin_api_service.exceptions.ResourceNotFoundException;
import com.example.admin_api_service.models.feeAndLimits.TransactionLimitOverride;
import com.example.admin_api_service.repository.TransactionLimitOverrideRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Service
@Transactional
public class TransactionLimitOverrideServiceImpl implements ITransactionLimitOverrideService {

    private final TransactionLimitOverrideRepository overrideRepository;
    private final ITransactionLimitService limitService;

    public TransactionLimitOverrideServiceImpl(TransactionLimitOverrideRepository overrideRepository, @Lazy ITransactionLimitService limitService) {
        this.overrideRepository = overrideRepository;
        this.limitService = limitService;
    }

    @Override
    public TransactionLimitOverride applyOverride(String transactionLimitId, Long userId, Long walletId,
                                                   String reason, String internalNote,
                                                   BigDecimal singleTransactionMax,
                                                   BigDecimal singleTransactionMin,
                                                   BigDecimal dailyMax, Integer dailyCountMax,
                                                   BigDecimal weeklyMax, Integer weeklyCountMax,
                                                   BigDecimal monthlyMax, Integer monthlyCountMax,
                                                   String appliedBy, String ipAddress,
                                                   LocalDateTime expiresAt) {
        limitService.getLimitById(transactionLimitId); 

        // Prevent duplicate active override for same user + limit
        boolean exists = overrideRepository
                .existsByTransactionLimitIdAndUserIdAndStatus(
                        transactionLimitId, userId, LimitOverrideStatus.ACTIVE);
        if (exists) {
            throw new ConflictException("An active override already exists for this user on limit "
                    + transactionLimitId);
        }

        TransactionLimitOverride override = new TransactionLimitOverride();
        override.setTransactionLimitId(transactionLimitId);
        override.setUserId(userId);
        override.setWalletId(walletId);
        override.setReason(reason);
        override.setInternalNote(internalNote);
        override.setSingleTransactionMax(singleTransactionMax);
        override.setSingleTransactionMin(singleTransactionMin);
        override.setDailyMax(dailyMax);
        override.setDailyCountMax(dailyCountMax);
        override.setWeeklyMax(weeklyMax);
        override.setWeeklyCountMax(weeklyCountMax);
        override.setMonthlyMax(monthlyMax);
        override.setMonthlyCountMax(monthlyCountMax);
        override.setAppliedBy(appliedBy);
        override.setAppliedAt(LocalDateTime.now());
        override.setIpAddress(ipAddress);
        override.setExpiresAt(expiresAt);
        override.setStatus(LimitOverrideStatus.ACTIVE);
        return overrideRepository.save(override);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionLimitOverride getOverrideById(String overrideId) {
        return overrideRepository.findById(overrideId)
                .orElseThrow(() -> new ResourceNotFoundException("TransactionLimitOverride", "id", overrideId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionLimitOverride> getAllOverrides(Pageable pageable) {
        return overrideRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionLimitOverride> getOverridesByStatus(LimitOverrideStatus status, Pageable pageable) {
        return overrideRepository.findAllByStatus(status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionLimitOverride> getOverridesByUser(Long userId) {
        return overrideRepository.findAllByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TransactionLimitOverride> getActiveOverrideForUser(Long userId, Long walletId) {
        if (walletId != null) {
            return overrideRepository.findByUserIdAndWalletIdAndStatus(
                    userId, walletId, LimitOverrideStatus.ACTIVE)
                    .filter(ov -> ov.getExpiresAt() == null
                            || ov.getExpiresAt().isAfter(LocalDateTime.now()));
        }
        return overrideRepository.findTopByUserIdAndStatusOrderByAppliedAtDesc(
                userId, LimitOverrideStatus.ACTIVE)
                .filter(ov -> ov.getExpiresAt() == null
                        || ov.getExpiresAt().isAfter(LocalDateTime.now()));
    }

    @Override
    public TransactionLimitOverride approveOverride(String overrideId, String approvedBy) {
        TransactionLimitOverride override = getOverrideById(overrideId);
        if (override.getStatus() != LimitOverrideStatus.ACTIVE) {
            throw new ConflictException("Override is not in an approvable state: " + override.getStatus());
        }
        override.setApprovedBy(approvedBy);
        override.setApprovedAt(LocalDateTime.now());
        override.setUpdatedOn(LocalDateTime.now());
        return overrideRepository.save(override);
    }

    @Override
    public TransactionLimitOverride revokeOverride(String overrideId, String revokedBy,
                                                    String revokeReason) {
        TransactionLimitOverride override = getOverrideById(overrideId);
        if (override.getStatus() != LimitOverrideStatus.ACTIVE) {
            throw new ConflictException("Override is not active. Current status: " + override.getStatus());
        }
        override.setStatus(LimitOverrideStatus.REVOKED);
        override.setRevokedBy(revokedBy);
        override.setRevokedAt(LocalDateTime.now());
        override.setRevokeReason(revokeReason);
        override.setUpdatedOn(LocalDateTime.now());
        return overrideRepository.save(override);
    }

    @Override
    @Scheduled(fixedDelay = 300000) // every 5 minutes
    public void expireStaleOverrides() {
        List<TransactionLimitOverride> expired = overrideRepository
                .findAllByStatusAndExpiresAtBefore(LimitOverrideStatus.ACTIVE, LocalDateTime.now());
        expired.forEach(ov -> {
            ov.setStatus(LimitOverrideStatus.EXPIRED);
            ov.setUpdatedOn(LocalDateTime.now());
        });
        if (!expired.isEmpty()) {
            overrideRepository.saveAll(expired);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal resolveEffectiveSingleMax(Long userId, Long walletId, BigDecimal baseLimitMax) {
        return getActiveOverrideForUser(userId, walletId)
                .map(ov -> ov.getSingleTransactionMax() != null
                        ? ov.getSingleTransactionMax() : baseLimitMax)
                .orElse(baseLimitMax);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal resolveEffectiveDailyMax(Long userId, Long walletId, BigDecimal baseDailyMax) {
        return getActiveOverrideForUser(userId, walletId)
                .map(ov -> ov.getDailyMax() != null ? ov.getDailyMax() : baseDailyMax)
                .orElse(baseDailyMax);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal resolveEffectiveMonthlyMax(Long userId, Long walletId, BigDecimal baseMonthlyMax) {
        return getActiveOverrideForUser(userId, walletId)
                .map(ov -> ov.getMonthlyMax() != null ? ov.getMonthlyMax() : baseMonthlyMax)
                .orElse(baseMonthlyMax);
    }
}