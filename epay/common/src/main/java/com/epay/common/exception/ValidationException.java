package com.epay.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import java.util.HashMap;
import java.util.Map;

@Getter
public class ValidationException extends BaseException {
    
    private final Map<String, String> fieldErrors;
    
    public ValidationException(String message) {
        super(message, ErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST);
        this.fieldErrors = new HashMap<>();
    }
    
    public ValidationException(String message, Map<String, String> fieldErrors) {
        super(message, ErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST);
        this.fieldErrors = fieldErrors != null ? fieldErrors : new HashMap<>();
    }
    
    public ValidationException addFieldError(String field, String error) {
        this.fieldErrors.put(field, error);
        return this;
    }
}
