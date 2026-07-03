package com.epay.common.exception;

import org.springframework.http.HttpStatus;

public class JwtAuthenticationException extends BaseException {

    public JwtAuthenticationException(String message, String errorCode) {
        super(message, errorCode, HttpStatus.UNAUTHORIZED);
    }

    public JwtAuthenticationException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, HttpStatus.UNAUTHORIZED, cause);
    }
    
    public static JwtAuthenticationException tokenExpired() {
        return new JwtAuthenticationException("Access token has expired", ErrorCode.TOKEN_EXPIRED);
    }
    
    public static JwtAuthenticationException tokenInvalid() {
        return new JwtAuthenticationException("Invalid access token", ErrorCode.TOKEN_INVALID);
    }
    
    public static JwtAuthenticationException tokenMissing() {
        return new JwtAuthenticationException("Access token is required", ErrorCode.UNAUTHORIZED_ACCESS);
    }
}
