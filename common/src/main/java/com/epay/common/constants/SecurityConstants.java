package com.epay.common.constants;

public final class SecurityConstants {

    // JWT
    public static final String TOKEN_PREFIX        = "Bearer ";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String JWT_CLAIM_USER_ID   = "userId";
    public static final String JWT_CLAIM_ROLE      = "role";
    public static final String JWT_CLAIM_EMAIL     = "email";
    public static final long   ACCESS_TOKEN_TTL_MS = 15 * 60 * 1000L;        // 15 minutes
    public static final long   REFRESH_TOKEN_TTL_MS = 7 * 24 * 60 * 60 * 1000L; // 7 days

    // OTP
    public static final int    OTP_LENGTH          = 6;
    public static final long   OTP_TTL_MINUTES     = 10;
    public static final int    OTP_MAX_ATTEMPTS    = 5;
    public static final long   OTP_COOLDOWN_SECONDS = 120; // 2 minutes between resend

    // Account lockout
    public static final int    MAX_FAILED_LOGINS   = 5;
    public static final long   LOCK_DURATION_MINUTES = 30;

    // Public endpoints — no JWT required
    public static final String[] PUBLIC_ENDPOINTS = {
        "/auth/register",
        "/auth/login",
        "/auth/verify-otp",
        "/auth/forgot-password",
        "/auth/reset-password",
        "/auth/forgot-username",
        "/auth/refresh-token",
        "/actuator/health",
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html"
    };

    private SecurityConstants() {}
}
