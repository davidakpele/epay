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

    private final IJwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public JwtAuthenticationFilter(IJwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
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
            final String jwt = authHeader.substring(7).trim();
            final String username = jwtService.extractUsername(jwt);
            final List<String> roles = jwtService.extractRoles(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails;
                if (roles.contains("ADMIN") || roles.contains("SUPER_ADMIN")) {
                    UserDetails tempDetails = org.springframework.security.core.userdetails.User.builder()
                            .username(username)
                            .password("")
                            .authorities(roles.stream()
                                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                    .toList())
                            .build();

                    if (!jwtService.isTokenValid(jwt, tempDetails)) {
                        handleAuthenticationError(response, "Invalid or expired token");
                        return;
                    }

                    userDetails = tempDetails;

                } else {
                    userDetails = userDetailsService.loadUserByUsername(username);
                    if (!jwtService.isTokenValid(jwt, userDetails)) {
                        handleAuthenticationError(response, "Invalid or expired token");
                        return;
                    }
                }

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

            filterChain.doFilter(request, response);

        } catch (JwtAuthenticationException e) {
            handleAuthenticationError(response, e.getMessage());
        } catch (UsernameNotFoundException e) {
            handleAuthenticationError(response, "User not found");
        } catch (ServletException | IOException e) {
            handleAuthenticationError(response, "Authentication failed");
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
               path.startsWith("/css/") ||
               path.startsWith("/js/") ||
               path.startsWith("/images/") ||
               path.equals("/favicon.ico") ||
               (path.startsWith("/user/username/") && request.getMethod().equals("GET")) ||
               (path.matches("/user/\\d+") && request.getMethod().equals("GET")) ||
               path.startsWith("/cache/users/") ||
               path.startsWith("/user/list");
    }

    private void handleAuthenticationError(HttpServletResponse response, String message) throws IOException {
        com.epay.common.exception.ErrorResponse body =
                com.epay.common.exception.ErrorResponse.builder()
                .success(false)
                .errorId(java.util.UUID.randomUUID().toString())
                .errorCode(com.epay.common.exception.ErrorCode.UNAUTHORIZED_ACCESS)
                .message(message)
                .timestamp(java.time.Instant.now())
                .status(HttpServletResponse.SC_UNAUTHORIZED)
                .build();

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), body);
    }
}
