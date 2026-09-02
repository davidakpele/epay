package com.epay.common.exception;

import org.springframework.http.HttpStatus;

public class DepositException extends BaseException {
    
    public DepositException(String message, String errorCode) {
        super(message, errorCode, HttpStatus.BAD_REQUEST);
    }
    
    public DepositException(String message, String errorCode, HttpStatus status) {
        super(message, errorCode, status);
    }
    
    public static DepositException notFound(String reference) {
        return new DepositException("Deposit not found", ErrorCode.DEPOSIT_NOT_FOUND);
    }
    
    public static DepositException alreadyProcessed(String reference) {
        return new DepositException("Deposit has already been processed", ErrorCode.DEPOSIT_ALREADY_PROCESSED);
    }
    
    public static DepositException verificationFailed() {
        return new DepositException("Deposit verification failed", ErrorCode.DEPOSIT_VERIFICATION_FAILED);
    }
    
    public static DepositException gatewayError() {
        return new DepositException("Payment gateway error. Please try again", 
            ErrorCode.DEPOSIT_GATEWAY_ERROR, HttpStatus.BAD_GATEWAY);
    }
}
