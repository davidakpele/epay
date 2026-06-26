package com.example.admin_api_service.security;


import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {
    
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    
    // Rate limits
    private static final int LOGIN_RATE_LIMIT = 5; // 5 attempts per minute
    private static final int LOGIN_BURST_LIMIT = 10; // Allow bursts up to 10
    private static final int REGISTER_RATE_LIMIT = 3; // 3 registrations per hour
    private static final int GENERAL_RATE_LIMIT = 100; // 100 requests per minute
    
    private Bucket createLoginBucket() {
        return Bucket.builder()
            .addLimit(Bandwidth.classic(LOGIN_BURST_LIMIT, 
                Refill.intervally(LOGIN_RATE_LIMIT, Duration.ofMinutes(1))))
            .build();
    }
    
    private Bucket createRegisterBucket() {
        return Bucket.builder()
            .addLimit(Bandwidth.classic(REGISTER_RATE_LIMIT, 
                Refill.intervally(REGISTER_RATE_LIMIT, Duration.ofHours(1))))
            .build();
    }
    
    private Bucket createGeneralBucket() {
        return Bucket.builder()
            .addLimit(Bandwidth.classic(GENERAL_RATE_LIMIT, 
                Refill.intervally(GENERAL_RATE_LIMIT, Duration.ofMinutes(1))))
            .build();
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        String clientIp = getClientIp(request);
        String path = request.getRequestURI();
        String method = request.getMethod();
        Bucket bucket;
        String bucketKey;
        
        if (path.contains("/api/auth/login") && "POST".equals(method)) {
            bucketKey = clientIp + "_login";
            bucket = buckets.computeIfAbsent(bucketKey, k -> createLoginBucket());
        }else if (path.startsWith("/actuator")) {
            filterChain.doFilter(request, response);
            return;
        }else if (path.contains("/api/auth/register") && "POST".equals(method)) {
            bucketKey = clientIp + "_register";
            bucket = buckets.computeIfAbsent(bucketKey, k -> createRegisterBucket());
        } else {
            // For other endpoints, use general bucket
            bucketKey = clientIp + "_general";
            bucket = buckets.computeIfAbsent(bucketKey, k -> createGeneralBucket());
        }
        
        // Try to consume a token from the bucket
        if (bucket.tryConsume(1)) {
            // Success - add headers for client information
            addRateLimitHeaders(response, bucket);
            filterChain.doFilter(request, response);
        } else {
            // Rate limit exceeded
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.setHeader("Retry-After", getRetryAfterTime(path));
            response.getWriter().write(getRateLimitExceededMessage(path));
            return; // IMPORTANT: Stop further processing
        }
    }
    
    private void addRateLimitHeaders(HttpServletResponse response, Bucket bucket) {
        // Add rate limit headers for transparency
        response.setHeader("X-Rate-Limit-Remaining", 
            String.valueOf(bucket.getAvailableTokens()));
        
        // You might need to track these values separately
        if (response.containsHeader("X-Rate-Limit-Limit")) {
            response.setHeader("X-Rate-Limit-Limit", String.valueOf(LOGIN_RATE_LIMIT));
        }
    }
    
    private String getRetryAfterTime(String path) {
        if (path.contains("/api/auth/register")) {
            return "3600"; // 1 hour in seconds
        } else if (path.contains("/api/auth/login")) {
            return "60"; // 1 minute in seconds
        }
        return "60";
    }
    
    private String getRateLimitExceededMessage(String path) {
        String endpoint = path.contains("/register") ? "registration" : 
                         path.contains("/login") ? "login" : "requests";
        
        return String.format(
            "{\"error\":\"Too many %s attempts. Please try again later.\"," +
            "\"code\":\"RATE_LIMIT_EXCEEDED\"}", endpoint);
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Optionally exclude certain paths from rate limiting
        String path = request.getServletPath();
        return path.contains("/public") || path.contains("/health");
    }
}