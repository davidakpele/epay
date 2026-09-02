package com.epay.common.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends BaseException {
    
    public UserNotFoundException(String message) {
        super(message, ErrorCode.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
    }
    
    public static UserNotFoundException withId(String userId) {
        return new UserNotFoundException("User not found");
    }
    
    public static UserNotFoundException withUsername(String username) {
        return new UserNotFoundException("User not found");
    }
}
