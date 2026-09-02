package com.epay.admin.service;

import com.epay.common.exception.*;
import com.epay.domain.admin.dto.AdminWalletDTO;
import com.epay.domain.admin.input.AdminSetPinRequest;
import com.epay.domain.admin.input.AdminWalletActionRequest;
import com.epay.domain.history.entity.TransactionHistory;
import com.epay.history.repository.TransactionHistoryRepository;
import com.epay.domain.wallet.entity.CurrencyBalance;
import com.epay.domain.wallet.entity.Wallet;
import com.epay.domain.wallet.entity.WalletSettings;
import com.epay.wallet.repository.WalletRepository;
import com.epay.wallet.repository.WalletSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminWalletManagementService {

    private final WalletRepository            walletRepository;
    private final WalletSettingsRepository    walletSettingsRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;
    private final PasswordEncoder              passwordEncoder;

    public AdminWalletDTO getWalletByUserId(Long userId) {
        Wallet wallet = requireWallet(userId);
        return toWalletDTO(wallet);
    }

    public Page<AdminWalletDTO> listAllWallets(Pageable pageable) {
        return walletRepository.findAllPaginated(pageable).map(this::toWalletDTO);
    }

    public Page<AdminWalletDTO> listFrozenWallets(Pageable pageable) {
        return walletRepository.findByActive(false, pageable).map(this::toWalletDTO);
    }

    @Transactional
    public void freezeWallet(Long userId, AdminWalletActionRequest request, Long adminId) {
        Wallet wallet = requireWallet(userId);
        if (!wallet.isActive()) {
            throw new BadRequestException("Wallet is already frozen", ErrorCode.INVALID_INPUT);
        }
        wallet.setActive(false);
        walletRepository.save(wallet);
        log.info("[Admin] Wallet FROZEN: userId={} reason='{}' by={}", userId, request.getReason(), adminId);
    }

    @Transactional
    public void unfreezeWallet(Long userId, AdminWalletActionRequest request, Long adminId) {
        Wallet wallet = requireWallet(userId);
        if (wallet.isActive()) {
            throw new BadRequestException("Wallet is not frozen", ErrorCode.INVALID_INPUT);
        }
        wallet.setActive(true);
        walletRepository.save(wallet);
        log.info("[Admin] Wallet UNFROZEN: userId={} reason='{}' by={}", userId, request.getReason(), adminId);
    }

    @Transactional
    public void setWalletPin(Long userId, AdminSetPinRequest request, Long adminId) {
        Wallet wallet = requireWallet(userId);

        WalletSettings settings = walletSettingsRepository.findByWalletId(wallet.getId())
                .orElseGet(() -> {
                    WalletSettings s = new WalletSettings();
                    s.setWallet(wallet);
                    return s;
                });

        settings.setPassword(passwordEncoder.encode(request.getPin()));
        settings.setIsSecure(true);
        walletSettingsRepository.save(settings);
        log.info("[Admin] Wallet PIN set: userId={} reason='{}' by={}", userId, request.getReason(), adminId);
    }

    @Transactional
    public void resetWalletPin(Long userId, AdminWalletActionRequest request, Long adminId) {
        Wallet wallet = requireWallet(userId);

        walletSettingsRepository.findByWalletId(wallet.getId()).ifPresent(settings -> {
            settings.setPassword(null);
            settings.setIsSecure(false);
            walletSettingsRepository.save(settings);
        });
        log.info("[Admin] Wallet PIN RESET: userId={} reason='{}' by={}", userId, request.getReason(), adminId);
    }

    public Page<TransactionHistory> getUserTransactions(Long userId, Pageable pageable) {
        if (!walletRepository.existsByUserId(userId))
            throw new ResourceNotFoundException("No wallet found for userId=" + userId);
        return transactionHistoryRepository.findByUserId(userId, pageable);
    }

    public Page<TransactionHistory> getUserTransactionsByStatus(Long userId, String status, Pageable pageable) {
        if (!walletRepository.existsByUserId(userId))
            throw new ResourceNotFoundException("No wallet found for userId=" + userId);
        return transactionHistoryRepository.findByUserIdAndStatus(userId, status.toUpperCase(), pageable);
    }

    public java.util.Map<String, Object> getWalletStats() {
        java.util.Map<String, Object> stats = new java.util.LinkedHashMap<>();
        stats.put("totalWallets",  walletRepository.count());
        stats.put("activeWallets", walletRepository.countActive());
        stats.put("frozenWallets", walletRepository.countFrozen());
        return stats;
    }

    private Wallet requireWallet(Long userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for userId=" + userId));
    }

    private AdminWalletDTO toWalletDTO(Wallet wallet) {
        boolean pinSet = walletSettingsRepository.findByWalletId(wallet.getId())
                .map(WalletSettings::isIsSecure).orElse(false);

        List<AdminWalletDTO.BalanceEntry> balances = wallet.getBalances().stream()
                .map(b -> AdminWalletDTO.BalanceEntry.builder()
                        .currency(b.getCurrencyCode())
                        .symbol(b.getCurrencySymbol())
                        .balance(b.getBalance())
                        .isDefault(b.isDefault())
                        .build())
                .toList();

        return AdminWalletDTO.builder()
                .walletId(wallet.getId())
                .userId(wallet.getUserId())
                .active(wallet.isActive())
                .pinSet(pinSet)
                .balances(balances)
                .createdAt(wallet.getCreatedAt())
                .updatedAt(wallet.getUpdatedAt())
                .build();
    }
}
