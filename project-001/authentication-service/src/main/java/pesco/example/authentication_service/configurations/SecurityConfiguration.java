package pesco.example.authentication_service.configurations;

import pesco.example.authentication_service.security.FirewallExceptionFilter;
import pesco.example.authentication_service.security.RateLimitingFilter;
import pesco.example.authentication_service.security.SecurityHeadersFilter;
import pesco.example.authentication_service.security.InputValidationFilter;
import pesco.example.authentication_service.security.BotDetectionFilter;
import pesco.example.authentication_service.utils.CustomAuthenticationEntryPoint;
import pesco.example.authentication_service.utils.JwtProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final JwtProperties jwtProperties;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final RateLimitingFilter rateLimitingFilter;
    private final BotDetectionFilter botDetectionFilter;

    public SecurityConfiguration(JwtAuthenticationFilter jwtAuthFilter, AuthenticationProvider authenticationProvider, JwtProperties jwtProperties, CustomAuthenticationEntryPoint authenticationEntryPoint, RateLimitingFilter rateLimitingFilter, BotDetectionFilter botDetectionFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.authenticationProvider = authenticationProvider;
        this.jwtProperties = jwtProperties;
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
            // Security Headers - Protection against XSS, Clickjacking, MIME sniffing
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self'; " +
                                    "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
                                    "style-src 'self' 'unsafe-inline'; " +
                                    "img-src 'self' data: https:; " +
                                    "font-src 'self' data:; " +
                                    "connect-src 'self'; " +
                                    "frame-ancestors 'none'")
                )
                .frameOptions(frame -> frame.deny())
                .xssProtection(xss -> xss
                    .headerValue(org.springframework.security.web.header.writers.XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)
                )
                .contentTypeOptions(contentType -> contentType.disable())
                .referrerPolicy(referrer -> referrer
                    .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                )
                .permissionsPolicy(permissions -> permissions
                    .policy("geolocation=(self), microphone=(), camera=(), payment=()")
                )
            )
            
            // Custom security filters
            .addFilterBefore(
                new FirewallExceptionFilter(),
                UsernamePasswordAuthenticationFilter.class
            )
            .addFilterBefore(
                botDetectionFilter,
                UsernamePasswordAuthenticationFilter.class
            )
            .addFilterBefore(
                new InputValidationFilter(),
                UsernamePasswordAuthenticationFilter.class
            )
            .addFilterBefore(
                new SecurityHeadersFilter(),
                UsernamePasswordAuthenticationFilter.class
            )
            .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**", "/error/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/user/username/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/user/{id}").permitAll()
                .requestMatchers("/cache/users/**").permitAll()
                .requestMatchers("/user/list").permitAll()
                .requestMatchers("/home/**").permitAll()
                .requestMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/swagger-resources/**",
                    "/webjars/**"
                ).permitAll()
                .requestMatchers("/css/**", "/js/**", "/image/**", "/favicon.ico").permitAll()
                .requestMatchers("/actuator/health", "/health", "/ping").permitAll()
                .requestMatchers(HttpMethod.POST, "/user/{id}/block").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.POST, "/user/{id}/lock").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/user/{id}").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.GET, "/user/{id}/edit").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.GET, "/user/{id}/view").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers("/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .anyRequest().authenticated()
            )
            
            // OAuth2 JWT configuration
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                    .decoder(jwtDecoder())
                )
                .authenticationEntryPoint(authenticationEntryPoint)
            )
            
            .authenticationProvider(authenticationProvider)
            
            // Exception handling
            .exceptionHandling(handling -> handling
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(customAccessDeniedHandler())
            )
            
            // Session management - stateless for JWT
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
            
        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecretKey());
        SecretKey secretKey = Keys.hmacShaKeyFor(keyBytes);
        return NimbusJwtDecoder.withSecretKey(secretKey).build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return converter;
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