package com.payrix.administrator.dtos;

import lombok.Data;
import java.util.List;

@Data
public class WalletSectionDTO {
    private Long walletId;
    private boolean hasTransferPin;
    private List<WalletBalanceDTO> walletBalances;



    public WalletSectionDTO() {
    }

    public WalletSectionDTO(Long walletId, boolean hasTransferPin, List<WalletBalanceDTO> walletBalances) {
        this.walletId = walletId;
        this.hasTransferPin = hasTransferPin;
        this.walletBalances = walletBalances;
    }

    public Long getWalletId() {
        return this.walletId;
    }

    public void setWalletId(Long walletId) {
        this.walletId = walletId;
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

    public List<WalletBalanceDTO> getWalletBalances() {
        return this.walletBalances;
    }

    public void setWalletBalances(List<WalletBalanceDTO> walletBalances) {
        this.walletBalances = walletBalances;
    }

}
