package com.example.admin_api_service.services;

import com.example.admin_api_service.Interfaces.IWalletFreezeService;
import com.example.admin_api_service.enums.FreezeReason;
import com.example.admin_api_service.enums.FreezeType;
import com.example.admin_api_service.enums.WalletFreezeStatus;
import com.example.admin_api_service.exceptions.ConflictException;
import com.example.admin_api_service.exceptions.ResourceNotFoundException;
import com.example.admin_api_service.models.userAndWalletManagement.WalletFreeze;
import com.example.admin_api_service.repository.WalletFreezeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class WalletFreezeServiceImpl implements IWalletFreezeService {

    private final WalletFreezeRepository walletFreezeRepository;

    public WalletFreezeServiceImpl(WalletFreezeRepository walletFreezeRepository) {
        this.walletFreezeRepository = walletFreezeRepository;
    }

    @Override
    public WalletFreeze freezeWallet(Long walletId, Long userId,
                                     FreezeType freezeType, FreezeReason freezeReason,
                                     String reasonNote, String externalReference,
                                     String frozenBy, String ipAddress,
                                     LocalDateTime expiresAt) {
        // Prevent duplicate active freezes of the same type
        boolean alreadyFrozen = walletFreezeRepository
                .existsByWalletIdAndFreezeTypeAndStatus(walletId, freezeType, WalletFreezeStatus.ACTIVE);
        if (alreadyFrozen) {
            throw new ConflictException("Wallet " + walletId + " already has an active "
                    + freezeType + " freeze");
        }

        WalletFreeze freeze = new WalletFreeze();
        freeze.setWalletId(walletId);
        freeze.setUserId(userId);
        freeze.setFreezeType(freezeType);
        freeze.setFreezeReason(freezeReason);
        freeze.setReasonNote(reasonNote);
        freeze.setExternalReference(externalReference);
        freeze.setFrozenBy(frozenBy);
        freeze.setFrozenAt(LocalDateTime.now());
        freeze.setIpAddress(ipAddress);
        freeze.setExpiresAt(expiresAt);
        freeze.setStatus(WalletFreezeStatus.ACTIVE);
        return walletFreezeRepository.save(freeze);
    }

    @Override
    public WalletFreeze unfreezeWallet(String freezeId, String unfrozenBy, String unfreezeNote) {
        WalletFreeze freeze = getFreezeById(freezeId);
        if (freeze.getStatus() != WalletFreezeStatus.ACTIVE) {
            throw new ConflictException("Freeze is not active. Current status: " + freeze.getStatus());
        }
        freeze.setStatus(WalletFreezeStatus.LIFTED);
        freeze.setUnfrozenBy(unfrozenBy);
        freeze.setUnfrozenAt(LocalDateTime.now());
        freeze.setUnfreezeNote(unfreezeNote);
        freeze.setUpdatedOn(LocalDateTime.now());
        return walletFreezeRepository.save(freeze);
    }

    @Override
    @Transactional(readOnly = true)
    public WalletFreeze getFreezeById(String freezeId) {
        return walletFreezeRepository.findById(freezeId)
                .orElseThrow(() -> new ResourceNotFoundException("WalletFreeze", "id", freezeId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WalletFreeze> getAllFreezes(Pageable pageable) {
        return walletFreezeRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WalletFreeze> getFreezesByStatus(WalletFreezeStatus status, Pageable pageable) {
        return walletFreezeRepository.findAllByStatus(status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WalletFreeze> getActiveFreezesByWallet(Long walletId) {
        return walletFreezeRepository.findAllByWalletIdAndStatus(walletId, WalletFreezeStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WalletFreeze> getActiveFreezesByUser(Long userId) {
        return walletFreezeRepository.findAllByUserIdAndStatus(userId, WalletFreezeStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WalletFreeze> getFreezesByWallet(Long walletId, Pageable pageable) {
        return walletFreezeRepository.findAllByWalletId(walletId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isWalletFrozen(Long walletId) {
        return walletFreezeRepository.existsByWalletIdAndFreezeTypeAndStatus(
                walletId, FreezeType.FULL, WalletFreezeStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isWalletDebitBlocked(Long walletId) {
        // Debit is blocked by FULL freeze or DEBIT_ONLY freeze
        return walletFreezeRepository.existsByWalletIdAndStatusAndFreezeTypeIn(
                walletId, WalletFreezeStatus.ACTIVE,
                List.of(FreezeType.FULL, FreezeType.DEBIT_ONLY));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isWalletCreditBlocked(Long walletId) {
        // Credit is blocked by FULL freeze or CREDIT_ONLY freeze
        return walletFreezeRepository.existsByWalletIdAndStatusAndFreezeTypeIn(
                walletId, WalletFreezeStatus.ACTIVE,
                List.of(FreezeType.FULL, FreezeType.CREDIT_ONLY));
    }

    @Override
    @Scheduled(fixedDelay = 300000) // every 5 minutes
    public void expireAutoFreezes() {
        List<WalletFreeze> expired = walletFreezeRepository
                .findAllByStatusAndExpiresAtBefore(WalletFreezeStatus.ACTIVE, LocalDateTime.now());
        expired.forEach(freeze -> {
            freeze.setStatus(WalletFreezeStatus.EXPIRED);
            freeze.setUpdatedOn(LocalDateTime.now());
        });
        if (!expired.isEmpty()) {
            walletFreezeRepository.saveAll(expired);
        }
    }
}
