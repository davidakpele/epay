package com.epay.common.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends BaseException {
    
    public ForbiddenException(String message) {
        super(message, ErrorCode.FORBIDDEN_ACCESS, HttpStatus.FORBIDDEN);
    }
    
    public ForbiddenException(String message, String errorCode) {
        super(message, errorCode, HttpStatus.FORBIDDEN);
    }
}
