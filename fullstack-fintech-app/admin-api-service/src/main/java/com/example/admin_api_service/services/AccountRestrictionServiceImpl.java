package com.example.admin_api_service.services;

import com.example.admin_api_service.Interfaces.IAccountRestrictionService;
import com.example.admin_api_service.enums.AccountRestrictionStatus;
import com.example.admin_api_service.enums.AccountRestrictionType;
import com.example.admin_api_service.enums.RestrictionScope;
import com.example.admin_api_service.exceptions.ConflictException;
import com.example.admin_api_service.exceptions.ResourceNotFoundException;
import com.example.admin_api_service.models.userAndWalletManagement.AccountRestriction;
import com.example.admin_api_service.repository.AccountRestrictionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AccountRestrictionServiceImpl implements IAccountRestrictionService {

    private final AccountRestrictionRepository restrictionRepository;

    public AccountRestrictionServiceImpl(AccountRestrictionRepository restrictionRepository) {
        this.restrictionRepository = restrictionRepository;
    }

    @Override
    public AccountRestriction applyRestriction(Long userId, Long walletId,
                                               AccountRestrictionType restrictionType,
                                               String reason, String internalNote,
                                               BigDecimal limitAmount, String limitCurrency,
                                               String externalReference,
                                               String appliedBy, String ipAddress,
                                               LocalDateTime expiresAt) {
        // Prevent duplicate active restriction of the same type for the same user
        boolean alreadyExists = restrictionRepository
                .existsByUserIdAndRestrictionTypeAndStatus(
                        userId, restrictionType, AccountRestrictionStatus.ACTIVE);
        if (alreadyExists) {
            throw new ConflictException("An active " + restrictionType
                    + " restriction already exists for userId: " + userId);
        }

        AccountRestriction restriction = new AccountRestriction();
        restriction.setUserId(userId);
        restriction.setWalletId(walletId);
        restriction.setRestrictionType(restrictionType);
        restriction.setScope(walletId != null ? RestrictionScope.WALLET : RestrictionScope.USER);
        restriction.setReason(reason);
        restriction.setInternalNote(internalNote);
        restriction.setLimitAmount(limitAmount);
        restriction.setLimitCurrency(limitCurrency);
        restriction.setExternalReference(externalReference);
        restriction.setAppliedBy(appliedBy);
        restriction.setAppliedAt(LocalDateTime.now());
        restriction.setIpAddress(ipAddress);
        restriction.setExpiresAt(expiresAt);
        restriction.setStatus(AccountRestrictionStatus.ACTIVE);
        return restrictionRepository.save(restriction);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountRestriction getRestrictionById(String restrictionId) {
        return restrictionRepository.findById(restrictionId)
                .orElseThrow(() -> new ResourceNotFoundException("AccountRestriction", "id", restrictionId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccountRestriction> getAllRestrictions(Pageable pageable) {
        return restrictionRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccountRestriction> getRestrictionsByStatus(AccountRestrictionStatus status, Pageable pageable) {
        return restrictionRepository.findAllByStatus(status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountRestriction> getActiveRestrictionsByUser(Long userId) {
        return restrictionRepository.findAllByUserIdAndStatus(userId, AccountRestrictionStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountRestriction> getActiveRestrictionsByWallet(Long walletId) {
        return restrictionRepository.findAllByWalletIdAndStatus(walletId, AccountRestrictionStatus.ACTIVE);
    }

    @Override
    public AccountRestriction liftRestriction(String restrictionId, String liftedBy, String liftNote) {
        AccountRestriction restriction = getRestrictionById(restrictionId);
        if (restriction.getStatus() != AccountRestrictionStatus.ACTIVE) {
            throw new ConflictException("Restriction is not active. Current status: " + restriction.getStatus());
        }
        restriction.setStatus(AccountRestrictionStatus.LIFTED);
        restriction.setLiftedBy(liftedBy);
        restriction.setLiftedAt(LocalDateTime.now());
        restriction.setLiftNote(liftNote);
        restriction.setUpdatedOn(LocalDateTime.now());
        return restrictionRepository.save(restriction);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasActiveRestriction(Long userId, AccountRestrictionType restrictionType) {
        return restrictionRepository.existsByUserIdAndRestrictionTypeAndStatus(
                userId, restrictionType, AccountRestrictionStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isLoginDisabled(Long userId) {
        return hasActiveRestriction(userId, AccountRestrictionType.LOGIN_DISABLED);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isTransactionDisabled(Long userId) {
        return hasActiveRestriction(userId, AccountRestrictionType.TRANSACTION_DISABLED);
    }

    @Override
    @Scheduled(fixedDelay = 300000) // every 5 minutes
    public void expireStaleRestrictions() {
        List<AccountRestriction> expired = restrictionRepository
                .findAllByStatusAndExpiresAtBefore(AccountRestrictionStatus.ACTIVE, LocalDateTime.now());
        expired.forEach(r -> {
            r.setStatus(AccountRestrictionStatus.EXPIRED);
            r.setUpdatedOn(LocalDateTime.now());
        });
        if (!expired.isEmpty()) {
            restrictionRepository.saveAll(expired);
        }
    }
}
