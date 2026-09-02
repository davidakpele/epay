package com.epay.wallet.adapter;

import com.epay.common.interfaces.IWalletPort;
import com.epay.domain.wallet.input.CreateWalletRequest;
import com.epay.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WalletServiceAdapter implements IWalletPort {

    private final WalletService walletService;

    @Override
    public void createWalletForUser(Long userId, String defaultCurrency) {
        CreateWalletRequest request = new CreateWalletRequest();
        request.setUserId(userId);
        request.setDefaultCurrency(defaultCurrency != null ? defaultCurrency.toUpperCase() : "NGN");
        walletService.createWallet(request);
    }
}
