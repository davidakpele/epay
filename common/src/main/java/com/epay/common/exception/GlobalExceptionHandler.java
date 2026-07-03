package com.epay.common.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex, WebRequest request) {
        logException(ex);
        
        ErrorResponse response;
        if (ex instanceof ValidationException validationEx) {
            response = ErrorResponse.from(ex, getPath(request), validationEx.getFieldErrors());
        } else if (ex instanceof RateLimitExceededException rateLimitEx) {
            response = ErrorResponse.from(ex, getPath(request));
            response.setRetryAfter(rateLimitEx.getRetryAfterSeconds());
        } else {
            response = ErrorResponse.from(ex, getPath(request));
        }
        
        return ResponseEntity.status(ex.getHttpStatus()).body(response);
    }

    
    @ExceptionHandler({UsernameNotFoundException.class, BadCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleAuthenticationFailure(
            Exception ex, WebRequest request) {
        log.warn("Authentication failed from IP: {}", getClientIp(request));
        
        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .errorId(UUID.randomUUID().toString())
                .errorCode(ErrorCode.INVALID_CREDENTIALS)
                .message("Invalid credentials")
                .timestamp(Instant.now())
                .path(getPath(request))
                .status(HttpStatus.UNAUTHORIZED.value())
                .build();
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex, WebRequest request) {
        log.warn("Access denied for path: {}", getPath(request));
        
        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .errorId(UUID.randomUUID().toString())
                .errorCode(ErrorCode.FORBIDDEN_ACCESS)
                .message("You do not have permission to access this resource")
                .timestamp(Instant.now())
                .path(getPath(request))
                .status(HttpStatus.FORBIDDEN.value())
                .build();
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, WebRequest request) {
        log.debug("Validation errors: {}", ex.getBindingResult().getErrorCount());
        
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            fieldErrors.put(field, message);
        });
        
        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .errorId(UUID.randomUUID().toString())
                .errorCode(ErrorCode.VALIDATION_ERROR)
                .message("Validation failed")
                .timestamp(Instant.now())
                .path(getPath(request))
                .status(HttpStatus.BAD_REQUEST.value())
                .fieldErrors(fieldErrors)
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        log.debug("Type mismatch: {}", ex.getMessage());
        
        String message = String.format("Invalid value for parameter '%s'", ex.getName());
        
        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .errorId(UUID.randomUUID().toString())
                .errorCode(ErrorCode.INVALID_INPUT)
                .message(message)
                .timestamp(Instant.now())
                .path(getPath(request))
                .status(HttpStatus.BAD_REQUEST.value())
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(RequestRejectedException.class)
    public ResponseEntity<ErrorResponse> handleRequestRejected(
            RequestRejectedException ex, WebRequest request) {
        log.warn("Request rejected - potential security threat: {}", sanitizeMessage(ex.getMessage()));
        
        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .errorId(UUID.randomUUID().toString())
                .errorCode(ErrorCode.INVALID_INPUT)
                .message("Request rejected due to security constraints")
                .timestamp(Instant.now())
                .path(getPath(request))
                .status(HttpStatus.BAD_REQUEST.value())
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotAcceptable(
            HttpMediaTypeNotAcceptableException ex, WebRequest request) {
        log.debug("Media type not acceptable");
        
        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .errorId(UUID.randomUUID().toString())
                .errorCode(ErrorCode.INVALID_INPUT)
                .message("Requested media type is not supported")
                .timestamp(Instant.now())
                .path(getPath(request))
                .status(HttpStatus.NOT_ACCEPTABLE.value())
                .build();
        
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(response);
    }
    
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            NoHandlerFoundException ex, WebRequest request) {
        log.debug("No handler found for: {} {}", ex.getHttpMethod(), ex.getRequestURL());
        
        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .errorId(UUID.randomUUID().toString())
                .errorCode(ErrorCode.RESOURCE_NOT_FOUND)
                .message("The requested endpoint was not found")
                .timestamp(Instant.now())
                .path(getPath(request))
                .status(HttpStatus.NOT_FOUND.value())
                .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobal(
            Exception ex, WebRequest request) {
        String errorId = UUID.randomUUID().toString();
        log.error("Unhandled exception [errorId={}]: {}", errorId, ex.getMessage(), ex);
        
        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .errorId(errorId)
                .errorCode(ErrorCode.INTERNAL_SERVER_ERROR)
                .message("An unexpected error occurred. Please contact support with error ID: " + errorId)
                .timestamp(Instant.now())
                .path(getPath(request))
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    private void logException(BaseException ex) {
        if (ex.getHttpStatus().is5xxServerError()) {
            log.error("Server error [errorId={}] [errorCode={}]: {}", 
                ex.getErrorId(), ex.getErrorCode(), ex.getMessage(), ex);
        } else if (ex.getHttpStatus().is4xxClientError()) {
            log.warn("Client error [errorId={}] [errorCode={}]: {}", 
                ex.getErrorId(), ex.getErrorCode(), ex.getMessage());
        }
    }
    
    private String getPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
    
    private String getClientIp(WebRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getHeader("X-Real-IP");
        }
        return ip != null ? ip : "unknown";
    }
    
    private String sanitizeMessage(String message) {
        if (message == null) return "Invalid request";
        return message.replaceAll("\\b\\d{16}\\b", "****")  
                      .replaceAll("\\b\\d{3}-\\d{2}-\\d{4}\\b", "***-**-****");
    }
}
