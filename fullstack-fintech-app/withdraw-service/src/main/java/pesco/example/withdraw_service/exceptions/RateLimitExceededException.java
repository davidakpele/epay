package pesco.example.withdraw_service.exceptions;

import java.util.concurrent.TimeUnit;

public class RateLimitExceededException extends RuntimeException {
    private final long capacity;
    private final long duration;
    private final TimeUnit timeUnit;
    private final long remainingTokens;

    public RateLimitExceededException(String message, long capacity, long duration, TimeUnit timeUnit, long remainingTokens) {
        super(message);
        this.capacity = capacity;
        this.duration = duration;
        this.timeUnit = timeUnit;
        this.remainingTokens = remainingTokens;
    }

    public long getCapacity() {
        return capacity;
    }

    public long getDuration() {
        return duration;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public long getRemainingTokens() {
        return remainingTokens;
    }
}