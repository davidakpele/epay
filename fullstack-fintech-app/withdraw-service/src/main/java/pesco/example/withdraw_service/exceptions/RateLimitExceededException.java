package pesco.example.withdraw_service.exceptions;

import lombok.Getter;
import java.util.concurrent.TimeUnit;

@Getter
public class RateLimitExceededException extends RuntimeException {
    
    private final int capacity;
    private final long duration;
    private final TimeUnit timeUnit;
    private final long remainingTokens;

    public RateLimitExceededException(String message, int capacity, long duration, 
                                       TimeUnit timeUnit, long remainingTokens) {
        super(message);
        this.capacity = capacity;
        this.duration = duration;
        this.timeUnit = timeUnit;
        this.remainingTokens = remainingTokens;
    }
}