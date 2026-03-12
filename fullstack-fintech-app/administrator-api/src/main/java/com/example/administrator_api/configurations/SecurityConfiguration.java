package com.example.administrator_api.configurations;

import com.example.administrator_api.security.CustomUserDetailsService;
import com.example.administrator_api.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
@EnableReactiveMethodSecurity
public class SecurityConfiguration {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfiguration(CustomUserDetailsService customUserDetailsService,
                                 JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
            .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
            .logout(ServerHttpSecurity.LogoutSpec::disable)

            .authorizeExchange(auth -> auth
                // ── Public endpoints ──────────────────────────────────
                .pathMatchers(
                    "/admin/api/auth/login",
                    "/admin/api/auth/register",
                    "/admin/api/auth/refresh",
                    "/admin/api/auth/logout",
                    "/admin/api/verify-user",
                    "/admin/api/username/**",
                    "/actuator/health",
                    "/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()
                // ── Role-protected endpoints ──────────────────────────
                .pathMatchers("/admin/dashboard").hasRole("ADMIN")
                .pathMatchers("/admin/users").hasRole("ADMIN")
                .pathMatchers("/admin/user/**").hasAnyRole("USER", "ADMIN")
                .pathMatchers("/admin/api/**").hasAnyRole("USER", "ADMIN")
                .anyExchange().authenticated()
            )

            // ── Exception handling ────────────────────────────────────
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((exchange, e) -> {
                    String path = exchange.getRequest().getPath().value();
                    ServerHttpResponse response = exchange.getResponse();
                    if (path.startsWith("/api/") || path.startsWith("/admin/api/")) {
                        response.setStatusCode(HttpStatus.UNAUTHORIZED);
                        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                        byte[] bytes = "{\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}"
                                .getBytes();
                        DataBuffer buffer = response.bufferFactory().wrap(bytes);
                        return response.writeWith(Mono.just(buffer));
                    }
                    response.setStatusCode(HttpStatus.FOUND);
                    response.getHeaders().setLocation(
                        java.net.URI.create("/admin/auth/login"));
                    return response.setComplete();
                })
                .accessDeniedHandler((exchange, e) -> {
                    String path = exchange.getRequest().getPath().value();
                    ServerHttpResponse response = exchange.getResponse();
                    if (path.startsWith("/api/") || path.startsWith("/admin/api/")) {
                        response.setStatusCode(HttpStatus.FORBIDDEN);
                        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                        byte[] bytes = "{\"error\":\"Forbidden\",\"message\":\"Access denied\"}"
                                .getBytes();
                        DataBuffer buffer = response.bufferFactory().wrap(bytes);
                        return response.writeWith(Mono.just(buffer));
                    }
                    response.setStatusCode(HttpStatus.FOUND);
                    response.getHeaders().setLocation(
                        java.net.URI.create("/admin/login?denied=true"));
                    return response.setComplete();
                })
            )

            // ── JWT filter runs before auth ───────────────────────────
            .addFilterBefore(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)

            .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public org.springframework.security.authentication.ReactiveAuthenticationManager reactiveAuthenticationManager() {
        var authManager = new org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager(
                customUserDetailsService);
        authManager.setPasswordEncoder(passwordEncoder());
        return authManager;
    }
}