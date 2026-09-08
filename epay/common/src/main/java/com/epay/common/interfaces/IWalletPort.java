package com.epay.common.interfaces;

public interface IWalletPort {

    void createWalletForUser(Long userId, String defaultCurrency);
}
