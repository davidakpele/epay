package com.epay.common.exception;

import org.springframework.http.HttpStatus;

public class AuthorizationException extends BaseException {
    
    public AuthorizationException(String message) {
        super(message, ErrorCode.FORBIDDEN_ACCESS, HttpStatus.FORBIDDEN);
    }
    
    public AuthorizationException(String message, String errorCode) {
        super(message, errorCode, HttpStatus.FORBIDDEN);
    }
}
