package com.epay.common.exception;

import org.springframework.http.HttpStatus;

public class BusinessRuleViolationException extends BaseException {
    
    public BusinessRuleViolationException(String message) {
        super(message, ErrorCode.BUSINESS_RULE_VIOLATION, HttpStatus.UNPROCESSABLE_ENTITY);
    }
    
    public BusinessRuleViolationException(String message, String errorCode) {
        super(message, errorCode, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
