package com.epay.common.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends BaseException {
    
    public BadRequestException(String message) {
        super(message, ErrorCode.INVALID_INPUT, HttpStatus.BAD_REQUEST);
    }
    
    public BadRequestException(String message, String errorCode) {
        super(message, errorCode, HttpStatus.BAD_REQUEST);
    }
}
