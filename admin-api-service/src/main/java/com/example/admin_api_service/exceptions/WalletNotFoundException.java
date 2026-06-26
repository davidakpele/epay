package com.example.admin_api_service.exceptions;

import com.example.admin_api_service.enums.Currency;

public class WalletNotFoundException extends RuntimeException {
    public WalletNotFoundException(Currency currency) {
        super("System wallet not found for currency: " + currency);
    }
}
