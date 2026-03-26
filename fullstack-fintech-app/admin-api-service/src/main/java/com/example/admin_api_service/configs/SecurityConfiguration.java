package com.example.admin_api_service.configs;

import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.authentication.AuthenticationProvider;
import com.example.admin_api_service.components.CustomAuthenticationEntryPoint;
import com.example.admin_api_service.security.BotDetectionFilter;
import com.example.admin_api_service.security.FirewallExceptionFilter;
import com.example.admin_api_service.security.InputValidationFilter;
import com.example.admin_api_service.security.JwtAuthenticationFilter;
import com.example.admin_api_service.security.RateLimitingFilter;
import com.example.admin_api_service.security.SecurityHeadersFilter;
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

    public SecurityConfiguration(JwtAuthenticationFilter jwtAuthFilter, AuthenticationProvider authenticationProvider, CustomAuthenticationEntryPoint authenticationEntryPoint, RateLimitingFilter rateLimitingFilter, BotDetectionFilter botDetectionFilter) {
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
        
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "X-Request-ID",
            "X-API-Version",
            "Cache-Control",
            "X-Forwarded-For",
            "X-Forwarded-Proto"
        ));
        
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Request-ID",
            "X-API-Version",
            "X-Rate-Limit-Limit",
            "X-Rate-Limit-Remaining",
            "X-Rate-Limit-Reset"
        ));
        
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    @SuppressWarnings("removal")
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .oauth2ResourceServer(AbstractHttpConfigurer::disable)  
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives(
                    "default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
                    "style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; " +
                    "font-src 'self' data:; connect-src 'self'; frame-ancestors 'none'"))
                .frameOptions(frame -> frame.deny())
                .xssProtection(xss -> xss.headerValue(
                    XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                .referrerPolicy(referrer -> referrer
                    .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                .permissionsPolicy(permissions -> permissions
                    .policy("geolocation=(self), microphone=(), camera=(), payment=()"))
            )
            .addFilterBefore(new FirewallExceptionFilter(),  UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(botDetectionFilter,             UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(new InputValidationFilter(),    UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(new SecurityHeadersFilter(),    UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(rateLimitingFilter,             UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthFilter,                  UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**", "/error/**").permitAll()
                .requestMatchers(
                    "/swagger-ui.html", "/swagger-ui/**",
                    "/v3/api-docs", "/v3/api-docs/**", "/webjars/**",
                    "/docs", "/docs/**" 
                ).permitAll()
                .requestMatchers("/admin/**").hasAnyAuthority("ADMIN", "SUPER_ADMIN")
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider)
            .exceptionHandling(handling -> handling
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(customAccessDeniedHandler())
            );
        return http.build();
    }


    @Bean
    public HttpFirewall httpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        // Block URL encoding and special characters
        firewall.setAllowSemicolon(false);
        firewall.setAllowUrlEncodedPercent(false);
        firewall.setAllowBackSlash(false);
        firewall.setAllowUrlEncodedSlash(false);
        firewall.setAllowUrlEncodedPeriod(false);
        firewall.setAllowUrlEncodedDoubleSlash(false);
        firewall.setAllowNull(false);
        
        // Block path traversal attempts
        firewall.setAllowUrlEncodedLineFeed(false);
        firewall.setAllowUrlEncodedCarriageReturn(false);
        firewall.setAllowUrlEncodedParagraphSeparator(false);
        
        // Restrict HTTP methods
        firewall.setAllowedHttpMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"
        ));
        
        // Block suspicious patterns in URLs
        firewall.setAllowedHostnames(hostname -> {
            String lower = hostname.toLowerCase();
            return !lower.contains("..") && 
                   !lower.contains("%2e") && 
                   !lower.contains("0x");
        });
        
        return firewall;
    }

    @Bean
    public org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer webSecurityCustomizer(
            HttpFirewall firewall
    ) {
        return web -> web.httpFirewall(firewall);
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