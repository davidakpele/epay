package com.epay.common.interfaces;

import java.math.BigDecimal;

public interface IDepositWalletPort {

    void creditWallet(Long userId, String currency, BigDecimal amount, String reference);

    Long getWalletId(Long userId);
    BigDecimal getBalance(Long userId, String currency);

    boolean walletExists(Long userId);

    boolean isCurrencySupported(String currency);

    String getCurrencySymbol(String currency);
}
