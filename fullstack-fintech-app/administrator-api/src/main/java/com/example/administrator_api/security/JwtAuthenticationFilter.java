package com.example.administrator_api.security;


import com.example.administrator_api.models.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public JwtAuthenticationFilter(JwtService jwtService,
                                   CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange,
                             @NonNull WebFilterChain chain) {

        String path = exchange.getRequest().getPath().value();

        // Skip filter for login endpoint
        if (path.contains("/login")) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        // No token — pass through (security config will block if needed)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }

        String jwt = authHeader.substring(7);

        String username;
        try {
            username = jwtService.extractUsername(jwt);
        } catch (JwtAuthenticationException ex) {
            return writeErrorResponse(exchange, ex.getStatus(), ex.getMessage());
        } catch (Exception ex) {
            return writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "Invalid token");
        }

        if (username == null) {
            return chain.filter(exchange);
        }

        return userDetailsService.findByUsername(username)
            .flatMap(userDetails -> {
                if (jwtService.validateToken(jwt) &&
                    jwtService.isTokenValid(jwt, (User) userDetails)) {

                    UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());

                    // Attach userId and username to request attributes
                    ServerWebExchange mutatedExchange = exchange.mutate()
                        .request(r -> r
                            .attribute("userId", jwtService.extractUserId(jwt))
                            .attribute("username", username))
                        .build();

                    return chain.filter(mutatedExchange)
                        .contextWrite(ReactiveSecurityContextHolder
                            .withAuthentication(authToken));
                }
                return writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "Token is not valid");
            })
            .onErrorResume(UsernameNotFoundException.class, ex ->
                writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, ex.getMessage()))
            .onErrorResume(JwtAuthenticationException.class, ex ->
                writeErrorResponse(exchange, ex.getStatus(), ex.getMessage()))
            .onErrorResume(Exception.class, ex ->
                writeErrorResponse(exchange, HttpStatus.INTERNAL_SERVER_ERROR,
                    "An unexpected error occurred"));
    }

    private Mono<Void> writeErrorResponse(ServerWebExchange exchange,
                                          HttpStatus status,
                                          String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {
            Map<String, Object> body = Map.of(
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message,
                "timestamp", LocalDateTime.now().toString()
            );
            byte[] bytes = objectMapper.writeValueAsBytes(body);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (Exception e) {
            return response.setComplete();
        }
    }
}