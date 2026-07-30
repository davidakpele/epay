package com.example.auth_user_service.exceptions;

import org.springframework.http.HttpStatus;

public class JwtAuthenticationException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    public JwtAuthenticationException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public JwtAuthenticationException(String message, HttpStatus status, String errorCode, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.errorCode = errorCode;
    }

    public JwtAuthenticationException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.errorCode = "JWT_ERROR";
    }

    public JwtAuthenticationException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.errorCode = "JWT_ERROR";
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
