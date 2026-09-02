package com.epay.common.exception;

import org.springframework.http.HttpStatus;

public class UserClientNotFoundException extends BaseException {
    
    public UserClientNotFoundException(String message) {
        super(message, ErrorCode.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
    }
}
