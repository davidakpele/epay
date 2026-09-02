package com.epay.common.exception;

import org.springframework.http.HttpStatus;

public class ExternalServiceException extends BaseException {
    
    public ExternalServiceException(String message) {
        super(message, ErrorCode.EXTERNAL_SERVICE_ERROR, HttpStatus.BAD_GATEWAY);
    }
    
    public ExternalServiceException(String message, String errorCode) {
        super(message, errorCode, HttpStatus.BAD_GATEWAY);
    }
    
    public ExternalServiceException(String message, Throwable cause) {
        super(message, ErrorCode.EXTERNAL_SERVICE_ERROR, HttpStatus.BAD_GATEWAY, cause);
    }
}
