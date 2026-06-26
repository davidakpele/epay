package pesco.example.withdraw_service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pesco.example.withdraw_service.dtos.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> handleUserNotFoundException(UserNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Error.createResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, "User not found"));
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleRateLimitExceeded(RateLimitExceededException ex) {
        log.warn("Rate limit exceeded: capacity={}, duration={} {}", 
            ex.getCapacity(), ex.getDuration(), ex.getTimeUnit());

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("retryAfter", calculateRetryAfter(ex));
        metadata.put("limit", ex.getCapacity());
        metadata.put("window", ex.getDuration() + " " + ex.getTimeUnit().name().toLowerCase());

        return ResponseEntity
            .status(HttpStatus.TOO_MANY_REQUESTS)
            .header("Retry-After", String.valueOf(calculateRetryAfter(ex)))
            .header("X-RateLimit-Limit", String.valueOf(ex.getCapacity()))
            .header("X-RateLimit-Remaining", String.valueOf(ex.getRemainingTokens()))
            .body(ApiResponse.error("Rate limit exceeded. Please try again later.", metadata));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("An unexpected error occurred"));
    }

    private long calculateRetryAfter(RateLimitExceededException ex) {
        return ex.getTimeUnit().toSeconds(ex.getDuration());
    }
}