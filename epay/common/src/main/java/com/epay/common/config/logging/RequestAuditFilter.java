package com.epay.common.config.logging;

import com.epay.common.config.components.IpExtractor;
import com.epay.common.config.object_mapper.RequestMetadata;
import com.epay.common.config.services.GeoLocationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Emits one structured JSON audit-log entry per HTTP request to the dedicated
 * {@code audit.request} logger.  The log is written AFTER the response so it
 * includes the final HTTP status code and elapsed time.
 *
 * <p>Fields captured per request:
 * <pre>
 *   requestId, timestamp, method, uri, queryString,
 *   status, latencyMs, authenticated, username, userId, roles,
 *   ip, device, location,
 *   userAgent, referer,
 *   blocked (true when rate-limit / security filter short-circuits)
 * </pre>
 *
 * <p>The filter runs AFTER {@code JwtAuthenticationFilter} so the
 * {@link SecurityContextHolder} is already populated when we read it.
 */
@RequiredArgsConstructor
public class RequestAuditFilter extends OncePerRequestFilter {

    /** Dedicated logger — routes only to the audit appender in logback-spring.xml */
    private static final Logger AUDIT = LoggerFactory.getLogger("audit.request");

    private static final List<String> SKIP_PATHS = List.of(
            "/actuator/**", "/favicon.ico", "/swagger-ui/**",
            "/v3/api-docs/**", "/docs/**", "/static/**", "/uploads/**"
    );

    private final IpExtractor       ipExtractor;
    private final GeoLocationService geoLocationService;
    private final ObjectMapper      objectMapper;
    private final AntPathMatcher    pathMatcher = new AntPathMatcher();

    // ── Filter ────────────────────────────────────────────────────────────────

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return SKIP_PATHS.stream().anyMatch(p -> pathMatcher.match(p, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        // Wrap response so we can read the final status after the chain completes
        ContentCachingResponseWrapper responseWrapper =
                new ContentCachingResponseWrapper(response);

        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        long start = System.currentTimeMillis();

        try {
            chain.doFilter(request, responseWrapper);
        } finally {
            long latencyMs = System.currentTimeMillis() - start;
            writeAuditLog(request, responseWrapper, requestId, latencyMs);
            responseWrapper.copyBodyToResponse();
            MDC.remove("requestId");
        }
    }

    // ── Audit log construction ─────────────────────────────────────────────────

    private void writeAuditLog(HttpServletRequest request,
                                ContentCachingResponseWrapper response,
                                String requestId,
                                long latencyMs) {
        try {
            // — Network / device
            RequestMetadata meta  = ipExtractor.extractMetadata(request);
            String          ip    = meta.ipAddress();
            String          geoHeader = request.getHeader("X-Geo-Location");
            String          location  = geoLocationService.resolve(geoHeader, ip);

            // — Auth context (populated by JwtAuthenticationFilter upstream)
            Authentication auth          = SecurityContextHolder.getContext().getAuthentication();
            boolean        authenticated = auth != null && auth.isAuthenticated()
                                           && !(auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken);
            String  username = authenticated ? auth.getName()     : "anonymous";
            String  roles    = authenticated
                    ? auth.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .reduce((a, b) -> a + "," + b)
                            .orElse("")
                    : "";

            // — Request details
            String method      = request.getMethod();
            String uri         = request.getRequestURI();
            String queryString = request.getQueryString();
            String userAgent   = request.getHeader("User-Agent");
            String referer     = request.getHeader("Referer");
            int    status      = response.getStatus();

            // — Detect blocked/rejected requests (4xx from security filters = blocked)
            boolean blocked = status == 401 || status == 403 || status == 429;

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("requestId",     requestId);
            entry.put("timestamp",     Instant.now().toString());
            entry.put("method",        method);
            entry.put("uri",           uri);
            entry.put("query",         queryString != null ? queryString : "");
            entry.put("status",        status);
            entry.put("latencyMs",     latencyMs);
            entry.put("authenticated", authenticated);
            entry.put("username",      username);
            entry.put("roles",         roles);
            entry.put("ip",            ip);
            entry.put("device",        meta.deviceType());
            entry.put("location",      location);
            entry.put("userAgent",     userAgent != null ? userAgent : "");
            entry.put("referer",       referer   != null ? referer   : "");
            entry.put("blocked",       blocked);

            AUDIT.info(objectMapper.writeValueAsString(entry));

        } catch (Exception ex) {
            AUDIT.warn("[RequestAuditFilter] Failed to write audit log: {}", ex.getMessage());
        }
    }
}
