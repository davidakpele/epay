package com.epay.common.exception;

import org.springframework.http.HttpStatus;

public class BusinessRuleViolationException extends BaseException {
    
    @SuppressWarnings("deprecation")
    public BusinessRuleViolationException(String message) {
        super(message, ErrorCode.BUSINESS_RULE_VIOLATION, HttpStatus.UNPROCESSABLE_ENTITY);
    }
    
    @SuppressWarnings("deprecation")
    public BusinessRuleViolationException(String message, String errorCode) {
        super(message, errorCode, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
