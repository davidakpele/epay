package com.epay.wallet.adapter;

import java.math.BigDecimal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import com.epay.common.exception.ResourceNotFoundException;
import com.epay.common.interfaces.IWithdrawWalletPort;
import com.epay.wallet.repository.SupportedCurrencyRepository;
import com.epay.wallet.repository.WalletRepository;
import com.epay.wallet.repository.WalletSettingsRepository;
import com.epay.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WalletWithdrawAdapter implements IWithdrawWalletPort {

    private final WalletService               walletService;
    private final WalletRepository            walletRepository;
    private final WalletSettingsRepository    walletSettingsRepository;
    private final SupportedCurrencyRepository currencyRepository;
    private final PasswordEncoder             passwordEncoder;

    @Override
    public void debitWallet(Long userId, String currency, BigDecimal amount, String reference) {
        Long walletId = getWalletId(userId);
        walletService.updateBalance(currency, amount.negate(), userId, walletId);
    }

    @Override
    public void refundWallet(Long userId, String currency, BigDecimal amount, String reference) {
        Long walletId = getWalletId(userId);
        walletService.updateBalance(currency, amount, userId, walletId);
    }

    @Override
    public BigDecimal getBalance(Long userId, String currency) {
        return walletRepository.findByUserId(userId)
                .map(w -> w.getBalanceAmount(currency))
                .orElse(BigDecimal.ZERO);
    }

    @Override
    public Long getWalletId(Long userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"))
                .getId();
    }

    @Override
    public boolean walletExists(Long userId) {
        return walletRepository.existsByUserId(userId);
    }

    @Override
    public boolean isCurrencySupported(String currency) {
        return currencyRepository.findByCodeIgnoreCase(currency)
                .map(c -> c.isActive()).orElse(false);
    }

    @Override
    public String getCurrencySymbol(String currency) {
        return currencyRepository.findByCodeIgnoreCase(currency)
                .map(c -> c.getSymbol()).orElse(currency);
    }


    @Override
    public boolean verifyPin(Long userId, String rawPin) {
        if (rawPin == null || rawPin.isBlank()) return false;

        Long walletId = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"))
                .getId();

        return walletSettingsRepository.findByWalletId(walletId)
                .filter(ws -> ws.getIsSecure())
                .map(ws -> passwordEncoder.matches(rawPin, ws.getPassword()))
                .orElse(false);
    }
}
