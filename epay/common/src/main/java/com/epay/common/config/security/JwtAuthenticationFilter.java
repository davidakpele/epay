package com.epay.common.config.security;

import com.epay.common.config.interfaces.IJwtService;
import com.epay.common.exception.JwtAuthenticationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final IJwtService        jwtService;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public JwtAuthenticationFilter(IJwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService         = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        if (isPublicEndpoint(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt     = authHeader.substring(7).trim();
            final String subject = jwtService.extractUsername(jwt);
            final List<String> roles = jwtService.extractRoles(jwt);

            String tokenType = jwtService.extractTokenType(jwt);
            if ("REFRESH".equalsIgnoreCase(tokenType) && !isRefreshEndpoint(request)) {
                writeError(response, HttpServletResponse.SC_UNAUTHORIZED,
                        "REFRESH tokens are not accepted on this endpoint. Use an ACCESS token.",
                        com.epay.common.exception.ErrorCode.UNAUTHORIZED_ACCESS);
                return;
            }

            String accountStatus = jwtService.extractAccountStatus(jwt);
            if ("LOCKED".equalsIgnoreCase(accountStatus)) {
                writeError(response, HttpServletResponse.SC_FORBIDDEN,
                        "This account is locked. Please contact support.",
                        com.epay.common.exception.ErrorCode.UNAUTHORIZED_ACCESS);
                return;
            }
            if ("INACTIVE".equalsIgnoreCase(accountStatus)) {
                writeError(response, HttpServletResponse.SC_FORBIDDEN,
                        "This account is inactive. Please verify your account.",
                        com.epay.common.exception.ErrorCode.UNAUTHORIZED_ACCESS);
                return;
            }

            if (subject != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails;

                if (roles.contains("ADMIN") || roles.contains("SUPER_USER")
                        || roles.contains("CUSTOMER_SERVICE") || roles.contains("EDITOR")) {
                    UserDetails tempDetails = org.springframework.security.core.userdetails.User.builder()
                            .username(subject)
                            .password("")
                            .authorities(roles.stream()
                                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                    .toList())
                            .build();

                    if (!jwtService.isTokenValid(jwt, tempDetails)) {
                        writeError(response, HttpServletResponse.SC_UNAUTHORIZED,
                                "Invalid or expired token",
                                com.epay.common.exception.ErrorCode.UNAUTHORIZED_ACCESS);
                        return;
                    }
                    userDetails = tempDetails;

                } else {
                    userDetails = userDetailsService.loadUserByUsername(subject);
                    if (!userDetails.isEnabled()) {
                        writeError(response, HttpServletResponse.SC_FORBIDDEN,
                                "This account is inactive. Please verify your account.",
                                com.epay.common.exception.ErrorCode.UNAUTHORIZED_ACCESS);
                        return;
                    }
                    if (!userDetails.isAccountNonLocked()) {
                        writeError(response, HttpServletResponse.SC_FORBIDDEN,
                                "This account is locked. Please contact support.",
                                com.epay.common.exception.ErrorCode.UNAUTHORIZED_ACCESS);
                        return;
                    }

                    if (!jwtService.isTokenValid(jwt, userDetails)) {
                        writeError(response, HttpServletResponse.SC_UNAUTHORIZED,
                                "Invalid or expired token",
                                com.epay.common.exception.ErrorCode.UNAUTHORIZED_ACCESS);
                        return;
                    }
                }

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

            filterChain.doFilter(request, response);

        } catch (JwtAuthenticationException e) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, e.getMessage(),
                    com.epay.common.exception.ErrorCode.UNAUTHORIZED_ACCESS);
        } catch (UsernameNotFoundException e) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "User not found",
                    com.epay.common.exception.ErrorCode.UNAUTHORIZED_ACCESS);
        } catch (ServletException | IOException e) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed",
                    com.epay.common.exception.ErrorCode.UNAUTHORIZED_ACCESS);
        }
    }

    private boolean isPublicEndpoint(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/auth/") ||
               path.startsWith("/error/") ||
               path.equals("/actuator/health") ||
               path.equals("/health") ||
               path.equals("/ping") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.equals("/docs") ||
               path.startsWith("/docs/") ||
               path.startsWith("/webhook/") ||
               path.startsWith("/css/") ||
               path.startsWith("/js/") ||
               path.startsWith("/images/") ||
               path.equals("/favicon.ico") ||
               (path.startsWith("/user/username/") && request.getMethod().equals("GET")) ||
               (path.matches("/user/\\d+") && request.getMethod().equals("GET")) ||
               path.startsWith("/cache/users/") ||
               path.startsWith("/user/list");
    }

    private boolean isRefreshEndpoint(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/auth/refresh") || path.equals("/auth/token/refresh");
    }

    private void writeError(HttpServletResponse response, int status,
                            String message, String errorCode) throws IOException {
        com.epay.common.exception.ErrorResponse body =
                com.epay.common.exception.ErrorResponse.builder()
                        .success(false)
                        .errorId(java.util.UUID.randomUUID().toString())
                        .errorCode(errorCode)
                        .message(message)
                        .timestamp(java.time.Instant.now())
                        .status(status)
                        .build();

        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), body);
    }
}