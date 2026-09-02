package com.epay.common.exception;

import org.springframework.http.HttpStatus;

public class WalletException extends BaseException {
    
    public WalletException(String message, String errorCode) {
        super(message, errorCode, HttpStatus.BAD_REQUEST);
    }
    
    public static WalletException notFound(String walletId) {
        return new WalletException("Wallet not found", ErrorCode.WALLET_NOT_FOUND);
    }
    
    public static WalletException locked() {
        return new WalletException("Wallet is locked and cannot perform transactions", ErrorCode.WALLET_LOCKED);
    }
    
    public static WalletException suspended() {
        return new WalletException("Wallet has been suspended", ErrorCode.WALLET_SUSPENDED);
    }
    
    public static WalletException invalidPin() {
        return new WalletException("Invalid transaction PIN", ErrorCode.INVALID_PIN);
    }
    
    public static WalletException pinLocked() {
        return new WalletException("Transaction PIN is locked due to too many failed attempts", ErrorCode.PIN_LOCKED);
    }
    
    public static WalletException dailyLimitExceeded() {
        return new WalletException("Daily transaction limit exceeded", ErrorCode.DAILY_LIMIT_EXCEEDED);
    }
    
    public static WalletException transactionLimitExceeded() {
        return new WalletException("Transaction amount exceeds maximum allowed limit", ErrorCode.TRANSACTION_LIMIT_EXCEEDED);
    }
    
    public static WalletException currencyNotSupported(String currency) {
        return new WalletException("Currency not supported: " + currency, ErrorCode.CURRENCY_NOT_SUPPORTED);
    }
}
