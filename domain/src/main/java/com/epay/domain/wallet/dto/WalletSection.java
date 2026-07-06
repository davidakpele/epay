package com.epay.domain.wallet.dto;

import java.util.List;
import lombok.Data;

@Data
public class WalletSection {
    private List<WalletBalanceDTO> wallet_balances;
    private Long walletId;
    private Long userId;
    private boolean hasTransferPin;

    public WalletSection() {
    }

    public List<WalletBalanceDTO> getWallet_balances() {
        return this.wallet_balances;
    }

    public void setWallet_balances(List<WalletBalanceDTO> wallet_balances) {
        this.wallet_balances = wallet_balances;
    }

    public Long getWalletId() {
        return this.walletId;
    }

    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public boolean isHasTransferPin() {
        return this.hasTransferPin;
    }

    public boolean getHasTransferPin() {
        return this.hasTransferPin;
    }

    public void setHasTransferPin(boolean hasTransferPin) {
        this.hasTransferPin = hasTransferPin;
    }

}
