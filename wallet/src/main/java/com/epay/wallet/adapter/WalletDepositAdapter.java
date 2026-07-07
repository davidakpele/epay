package com.epay.wallet.adapter;

import com.epay.common.exception.ResourceNotFoundException;
import com.epay.common.interfaces.IDepositWalletPort;
import com.epay.wallet.service.WalletService;
import com.epay.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class WalletDepositAdapter implements IDepositWalletPort {

    private final WalletService    walletService;
    private final WalletRepository walletRepository;

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
}
