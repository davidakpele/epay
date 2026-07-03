package com.epay.common.exception;

import org.springframework.http.HttpStatus;

public class AuthenticationException extends BaseException {
    
    public AuthenticationException(String message, String errorCode) {
        super(message, errorCode, HttpStatus.UNAUTHORIZED);
    }
    
    public AuthenticationException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, HttpStatus.UNAUTHORIZED, cause);
    }
}
