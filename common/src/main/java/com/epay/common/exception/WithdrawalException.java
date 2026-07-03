package com.epay.common.exception;

import org.springframework.http.HttpStatus;

public class WithdrawalException extends BaseException {
    
    public WithdrawalException(String message, String errorCode) {
        super(message, errorCode, HttpStatus.BAD_REQUEST);
    }
    
    public WithdrawalException(String message, String errorCode, HttpStatus status) {
        super(message, errorCode, status);
    }
    
    public static WithdrawalException notFound() {
        return new WithdrawalException("Withdrawal record not found", ErrorCode.WITHDRAWAL_NOT_FOUND);
    }
    
    public static WithdrawalException suspended() {
        return new WithdrawalException("Withdrawals are currently suspended", ErrorCode.WITHDRAWAL_SUSPENDED);
    }
    
    public static WithdrawalException limitExceeded() {
        return new WithdrawalException("Withdrawal limit exceeded", ErrorCode.WITHDRAWAL_LIMIT_EXCEEDED);
    }
    
    public static WithdrawalException invalidBankDetails() {
        return new WithdrawalException("Invalid bank account details provided", ErrorCode.INVALID_BANK_DETAILS);
    }
    
    public static WithdrawalException gatewayError() {
        return new WithdrawalException("Payout gateway error. Please try again", 
            ErrorCode.WITHDRAWAL_GATEWAY_ERROR, HttpStatus.BAD_GATEWAY);
    }
}
