package com.epay.common.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends BaseException {
    
    public ConflictException(String message) {
        super(message, ErrorCode.RESOURCE_CONFLICT, HttpStatus.CONFLICT);
    }
    
    public ConflictException(String message, String errorCode) {
        super(message, errorCode, HttpStatus.CONFLICT);
    }
}
