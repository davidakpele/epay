package com.epay.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    
    private boolean success;
    private String errorId;
    private String errorCode;
    private String message;
    private Instant timestamp;
    private String path;
    private Integer status;
    private Map<String, String> fieldErrors;
    private Long retryAfter;
    
    public static ErrorResponse from(BaseException ex, String path) {
        return ErrorResponse.builder()
                .success(false)
                .errorId(ex.getErrorId())
                .errorCode(ex.getErrorCode())
                .message(ex.getMessage())
                .timestamp(ex.getTimestamp())
                .path(path)
                .status(ex.getHttpStatus().value())
                .build();
    }
    
    public static ErrorResponse from(BaseException ex, String path, Map<String, String> fieldErrors) {
        return ErrorResponse.builder()
                .success(false)
                .errorId(ex.getErrorId())
                .errorCode(ex.getErrorCode())
                .message(ex.getMessage())
                .timestamp(ex.getTimestamp())
                .path(path)
                .status(ex.getHttpStatus().value())
                .fieldErrors(fieldErrors)
                .build();
    }
}
