package com.epay.wallet.adapter;

import com.epay.common.exception.ResourceNotFoundException;
import com.epay.common.interfaces.IDepositWalletPort;
import com.epay.wallet.repository.SupportedCurrencyRepository;
import com.epay.wallet.repository.WalletRepository;
import com.epay.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class WalletDepositAdapter implements IDepositWalletPort {

    private final WalletService               walletService;
    private final WalletRepository            walletRepository;
    private final SupportedCurrencyRepository currencyRepository;

    @Override
    public void creditWallet(Long userId, String currency, BigDecimal amount, String reference) {
        Long walletId = getWalletId(userId);
        walletService.updateBalance(currency, amount, userId, walletId);
    }

    @Override
    public Long getWalletId(Long userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user: " + userId))
                .getId();
    }

    @Override
    public BigDecimal getBalance(Long userId, String currency) {
        return walletRepository.findByUserId(userId)
                .map(w -> w.getBalanceAmount(currency))
                .orElse(BigDecimal.ZERO);
    }

    @Override
    public boolean walletExists(Long userId) {
        return walletRepository.existsByUserId(userId);
    }

    @Override
    public boolean isCurrencySupported(String currency) {
        return currencyRepository.findByCodeIgnoreCase(currency)
                .map(c -> c.isActive())
                .orElse(false);
    }

    @Override
    public String getCurrencySymbol(String currency) {
        return currencyRepository.findByCodeIgnoreCase(currency)
                .map(c -> c.getSymbol())
                .orElse(currency);
    }
}
