package com.epay.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.UUID;

@Getter
public abstract class BaseException extends RuntimeException {
    
    private final String errorId;
    private final String errorCode;
    private final HttpStatus httpStatus;
    private final Instant timestamp;
    private final transient Object[] args;
    
    protected BaseException(String message, String errorCode, HttpStatus httpStatus, Object... args) {
        super(message);
        this.errorId = UUID.randomUUID().toString();
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.timestamp = Instant.now();
        this.args = args;
    }
    
    protected BaseException(String message, String errorCode, HttpStatus httpStatus, Throwable cause, Object... args) {
        super(message, cause);
        this.errorId = UUID.randomUUID().toString();
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.timestamp = Instant.now();
        this.args = args;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        if (httpStatus.is5xxServerError()) {
            return super.fillInStackTrace();
        }
        return this;
    }
}
