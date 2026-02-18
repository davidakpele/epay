package com.pesco.wallet_service.configuration;

import com.pesco.wallet_service.security.BotDetectionFilter;
import com.pesco.wallet_service.security.FirewallExceptionFilter;
import com.pesco.wallet_service.security.InputValidationFilter;
import com.pesco.wallet_service.security.RateLimitingFilter;
import com.pesco.wallet_service.security.SecurityHeadersFilter;
import com.pesco.wallet_service.util.CustomAuthenticationEntryPoint;
import com.pesco.wallet_service.util.JwtProperties;
import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final RateLimitingFilter rateLimitingFilter;
    private final BotDetectionFilter botDetectionFilter;

    public SecurityConfiguration(JwtAuthenticationFilter jwtAuthFilter,
                                  AuthenticationProvider authenticationProvider,
                                  CustomAuthenticationEntryPoint authenticationEntryPoint,
                                  RateLimitingFilter rateLimitingFilter,
                                  BotDetectionFilter botDetectionFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.authenticationProvider = authenticationProvider;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.rateLimitingFilter = rateLimitingFilter;
        this.botDetectionFilter = botDetectionFilter;
    }

    @Bean
    @Primary
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization","Content-Type","X-Requested-With","Accept","Origin",
            "X-Request-ID","X-API-Version","Cache-Control","X-Forwarded-For",
            "X-Forwarded-Proto","Sec-WebSocket-Key","Sec-WebSocket-Version",
            "Sec-WebSocket-Protocol","Sec-WebSocket-Extensions"
        ));
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization","Content-Type","X-Request-ID","X-API-Version",
            "X-Rate-Limit-Limit","X-Rate-Limit-Remaining","X-Rate-Limit-Reset"
        ));
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives(
                    "default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
                    "style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; " +
                    "font-src 'self' data:; connect-src 'self'; frame-ancestors 'none'"
                ))
                .frameOptions(frame -> frame.deny())
                .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                .contentTypeOptions(contentType -> contentType.disable())
                .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                .permissionsPolicy(permissions -> permissions.policy("geolocation=(self), microphone=(), camera=(), payment=()"))
            )
            .addFilterBefore(new FirewallExceptionFilter(),   UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(botDetectionFilter,              UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(new InputValidationFilter(),     UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(new SecurityHeadersFilter(),     UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(rateLimitingFilter,              UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthFilter,                   UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth

                // ── Public — no token required ───────────────────────────────────
                .requestMatchers(
                    "/wallet/cache/**",
                    "/wallet/internal/debit/maintenance",
                    "/wallet/refund",
                    "/actuator/health",
                    "/health", "/ping",
                    "/css/**", "/js/**", "/images/**", "/favicon.ico",
                    "/swagger-ui.html", "/swagger-ui/**",
                    "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**"
                ).permitAll()

                // ── Admin-only paths ─────────────────────────────────────────────
                // JWT has "ADMIN" / "SUPER_ADMIN" → filter normalises to
                // "ROLE_ADMIN" / "ROLE_SUPER_ADMIN" → hasAuthority checks match
                .requestMatchers("/actuator/**", "/admin/**")
                    .hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPER_ADMIN")

                // ── Wallet — all authenticated roles ────────────────────────────
                .requestMatchers("/wallet/**")
                    .hasAnyAuthority("ROLE_USER", "ROLE_ADMIN", "ROLE_SUPER_ADMIN")

                // ── Everything else — any valid token ────────────────────────────
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider)
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(customAccessDeniedHandler())
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/ws/**");
    }

    @Bean
    public AccessDeniedHandler customAccessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(
                "{\"error\":\"Access Denied\"," +
                "\"message\":\"You do not have permission to access this resource\"," +
                "\"status\":403}"
            );
        };
    }
}