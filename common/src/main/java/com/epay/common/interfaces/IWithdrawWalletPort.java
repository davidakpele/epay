package com.epay.common.interfaces;

import java.math.BigDecimal;

public interface IWithdrawWalletPort {
    void debitWallet(Long userId, String currency, BigDecimal amount, String reference);

    void refundWallet(Long userId, String currency, BigDecimal amount, String reference);

    BigDecimal getBalance(Long userId, String currency);

    Long getWalletId(Long userId);

    boolean walletExists(Long userId);

    boolean isCurrencySupported(String currency);

    String getCurrencySymbol(String currency);

    boolean verifyPin(Long userId, String rawPin);
}