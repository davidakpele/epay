package com.epay.common.exception;

import org.springframework.http.HttpStatus;

public class InsufficientBalanceException extends BaseException {
    
    public InsufficientBalanceException(String message) {
        super(message, ErrorCode.INSUFFICIENT_BALANCE, HttpStatus.BAD_REQUEST);
    }
    
    public InsufficientBalanceException(String currency, double required, double available) {
        super(
            String.format("Insufficient balance. Required: %s %.2f, Available: %s %.2f", 
                currency, required, currency, available),
            ErrorCode.INSUFFICIENT_BALANCE,
            HttpStatus.BAD_REQUEST,
            currency, required, available
        );
    }
}
