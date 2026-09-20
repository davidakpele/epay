package com.epay.common.config.security;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class SecurityHeadersFilter extends OncePerRequestFilter {

    private static final String CSP = String.join("; ",
            "default-src 'self'",
            "base-uri 'self'",
            "font-src 'self' https: data:",
            "form-action 'self'",
            "frame-ancestors 'self'",
            "img-src 'self' data:",
            "object-src 'none'",
            "script-src 'self'",
            "script-src-attr 'none'",
            "style-src 'self' https: 'unsafe-inline'",
            "upgrade-insecure-requests");

    private static final String PERMISSIONS_POLICY = String.join(", ",
            "accelerometer=()",
            "ambient-light-sensor=()",
            "autoplay=()",
            "battery=()",
            "camera=()",
            "display-capture=()",
            "encrypted-media=()",
            "fullscreen=(self)",
            "geolocation=()",
            "gyroscope=()",
            "magnetometer=()",
            "microphone=()",
            "midi=()",
            "payment=()",
            "usb=()",
            "picture-in-picture=(self)");

    private static final String HSTS = "max-age=31536000; includeSubDomains";

    private static final List<String> CROSS_ORIGIN_PATHS = List.of("/uploads/images/", "/image/", "/static/");

    private static final Pattern SAFE_REQUEST_ID = Pattern.compile("^[A-Za-z0-9._-]{8,64}$");

    @Value("${INSTANCE_ID:unknown}")
    private String instanceId;

    @Value("${epay.security.api-version:1.0}")
    private String apiVersion;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        response.setHeader("Content-Security-Policy", CSP);
        response.setHeader("Cross-Origin-Opener-Policy", "same-origin");
        response.setHeader("Cross-Origin-Resource-Policy",
                isCrossOriginAsset(request) ? "cross-origin" : "same-origin");
        response.setHeader("Origin-Agent-Cluster", "?1");
        response.setHeader("Referrer-Policy", "no-referrer");
        response.setHeader("Strict-Transport-Security", HSTS);
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-DNS-Prefetch-Control", "off");
        response.setHeader("X-Download-Options", "noopen");
        response.setHeader("X-Frame-Options", "SAMEORIGIN");
        response.setHeader("X-Permitted-Cross-Domain-Policies", "none");
        response.setHeader("X-XSS-Protection", "0");
        response.setHeader("Permissions-Policy", PERMISSIONS_POLICY);

        response.setHeader("X-API-Version", apiVersion);
        response.setHeader("X-Instance-ID", instanceId);
        response.setHeader("X-Request-Id", resolveRequestId(request));

        filterChain.doFilter(request, response);
    }

    private boolean isCrossOriginAsset(HttpServletRequest request) {
        String path = request.getRequestURI();
        for (String prefix : CROSS_ORIGIN_PATHS) {
            if (path.startsWith(prefix)) return true;
        }
        return false;
    }
    
    private String resolveRequestId(HttpServletRequest request) {
        String incoming = request.getHeader("X-Request-Id");
        if (incoming != null && SAFE_REQUEST_ID.matcher(incoming).matches()) {
            return incoming;
        }
        return UUID.randomUUID().toString();
    }
}