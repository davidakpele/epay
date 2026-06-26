package com.example.admin_api_service.services;

import com.example.admin_api_service.Interfaces.IAccountFlagService;
import com.example.admin_api_service.enums.AccountFlagSeverity;
import com.example.admin_api_service.enums.AccountFlagStatus;
import com.example.admin_api_service.enums.AccountFlagType;
import com.example.admin_api_service.exceptions.ConflictException;
import com.example.admin_api_service.exceptions.ResourceNotFoundException;
import com.example.admin_api_service.models.userAndWalletManagement.AccountFlag;
import com.example.admin_api_service.repository.AccountFlagRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AccountFlagServiceImpl implements IAccountFlagService {

    private final AccountFlagRepository accountFlagRepository;

    public AccountFlagServiceImpl(AccountFlagRepository accountFlagRepository) {
        this.accountFlagRepository = accountFlagRepository;
    }

    @Override
    public AccountFlag raiseSystemFlag(Long userId, Long walletId, AccountFlagType flagType,
                                       AccountFlagSeverity severity, String title, String description,
                                       String triggerRule, String evidence, String ipAddress) {
        AccountFlag flag = buildFlag(userId, walletId, flagType, severity, title, description, ipAddress);
        flag.setSource("SYSTEM");
        flag.setTriggerRule(triggerRule);
        flag.setEvidence(evidence);
        return accountFlagRepository.save(flag);
    }

    @Override
    public AccountFlag raiseManualFlag(Long userId, Long walletId, AccountFlagType flagType,
                                       AccountFlagSeverity severity, String title, String description,
                                       String flaggedBy, String ipAddress) {
        AccountFlag flag = buildFlag(userId, walletId, flagType, severity, title, description, ipAddress);
        flag.setSource("MANUAL");
        flag.setFlaggedBy(flaggedBy);
        return accountFlagRepository.save(flag);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountFlag getFlagById(String flagId) {
        return accountFlagRepository.findById(flagId)
                .orElseThrow(() -> new ResourceNotFoundException("AccountFlag", "id", flagId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccountFlag> getAllFlags(Pageable pageable) {
        return accountFlagRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccountFlag> getFlagsByStatus(AccountFlagStatus status, Pageable pageable) {
        return accountFlagRepository.findAllByStatus(status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccountFlag> getFlagsBySeverity(AccountFlagSeverity severity, Pageable pageable) {
        return accountFlagRepository.findAllBySeverity(severity, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountFlag> getOpenFlagsForUser(Long userId) {
        return accountFlagRepository.findAllByUserIdAndStatusIn(
                userId, List.of(AccountFlagStatus.OPEN, AccountFlagStatus.UNDER_INVESTIGATION,
                        AccountFlagStatus.ESCALATED));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountFlag> getFlagsForWallet(Long walletId) {
        return accountFlagRepository.findAllByWalletId(walletId);
    }

    @Override
    public AccountFlag assignFlag(String flagId, String assignedTo) {
        AccountFlag flag = getFlagById(flagId);
        validateFlagIsOpen(flag);
        flag.setAssignedTo(assignedTo);
        flag.setAssignedAt(LocalDateTime.now());
        flag.setStatus(AccountFlagStatus.UNDER_INVESTIGATION);
        flag.setUpdatedOn(LocalDateTime.now());
        return accountFlagRepository.save(flag);
    }

    @Override
    public AccountFlag resolveFlag(String flagId, String resolvedBy, String resolutionNote) {
        AccountFlag flag = getFlagById(flagId);
        validateFlagIsOpen(flag);
        flag.setStatus(AccountFlagStatus.RESOLVED);
        flag.setResolvedBy(resolvedBy);
        flag.setResolvedAt(LocalDateTime.now());
        flag.setResolutionNote(resolutionNote);
        flag.setUpdatedOn(LocalDateTime.now());
        return accountFlagRepository.save(flag);
    }

    @Override
    public AccountFlag markFalsePositive(String flagId, String resolvedBy, String resolutionNote) {
        AccountFlag flag = getFlagById(flagId);
        validateFlagIsOpen(flag);
        flag.setStatus(AccountFlagStatus.FALSE_POSITIVE);
        flag.setResolvedBy(resolvedBy);
        flag.setResolvedAt(LocalDateTime.now());
        flag.setResolutionNote(resolutionNote);
        flag.setUpdatedOn(LocalDateTime.now());
        return accountFlagRepository.save(flag);
    }

    @Override
    public AccountFlag escalateFlag(String flagId, String escalatedBy) {
        AccountFlag flag = getFlagById(flagId);
        validateFlagIsOpen(flag);
        flag.setStatus(AccountFlagStatus.ESCALATED);
        flag.setUpdatedOn(LocalDateTime.now());
        return accountFlagRepository.save(flag);
    }

    @Override
    public AccountFlag linkToFreeze(String flagId, String freezeId) {
        AccountFlag flag = getFlagById(flagId);
        flag.setTriggeredFreezeId(freezeId);
        flag.setUpdatedOn(LocalDateTime.now());
        return accountFlagRepository.save(flag);
    }

    @Override
    public AccountFlag linkToRestriction(String flagId, String restrictionId) {
        AccountFlag flag = getFlagById(flagId);
        flag.setTriggeredRestrictionId(restrictionId);
        flag.setUpdatedOn(LocalDateTime.now());
        return accountFlagRepository.save(flag);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasOpenFlag(Long userId, AccountFlagType flagType) {
        return accountFlagRepository.existsByUserIdAndFlagTypeAndStatusIn(
                userId, flagType,
                List.of(AccountFlagStatus.OPEN, AccountFlagStatus.UNDER_INVESTIGATION,
                        AccountFlagStatus.ESCALATED));
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private AccountFlag buildFlag(Long userId, Long walletId, AccountFlagType flagType,
                                  AccountFlagSeverity severity, String title,
                                  String description, String ipAddress) {
        AccountFlag flag = new AccountFlag();
        flag.setUserId(userId);
        flag.setWalletId(walletId);
        flag.setFlagType(flagType);
        flag.setSeverity(severity);
        flag.setTitle(title);
        flag.setDescription(description);
        flag.setIpAddress(ipAddress);
        flag.setFlaggedAt(LocalDateTime.now());
        flag.setStatus(AccountFlagStatus.OPEN);
        return flag;
    }

    private void validateFlagIsOpen(AccountFlag flag) {
        if (flag.getStatus() == AccountFlagStatus.RESOLVED
                || flag.getStatus() == AccountFlagStatus.FALSE_POSITIVE
                || flag.getStatus() == AccountFlagStatus.CLOSED) {
            throw new ConflictException("Flag is already in a terminal state: " + flag.getStatus());
        }
    }
}