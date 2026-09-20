package com.epay.common.config.security;

import java.util.Arrays;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
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
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.epay.common.config.components.IpExtractor;
import com.epay.common.config.components.JwtProperties;
import com.epay.common.config.logging.RequestAuditFilter;
import com.epay.common.config.services.GeoLocationService;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletResponse;

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
    private final IpExtractor ipExtractor;
    private final GeoLocationService geoLocationService;
    private final ObjectMapper objectMapper;
    
    @Value("${epay.cors.allowed-origins:http://localhost:3000}")
    private List<String> allowedOrigins;

    public SecurityConfiguration(JwtAuthenticationFilter jwtAuthFilter,
                                  AuthenticationProvider authenticationProvider,
                                  JwtProperties jwtProperties,
                                  CustomAuthenticationEntryPoint authenticationEntryPoint,
                                  RateLimitingFilter rateLimitingFilter,
                                  BotDetectionFilter botDetectionFilter,
                                  IpExtractor ipExtractor,
                                  GeoLocationService geoLocationService,
                                  ObjectMapper objectMapper) {
        this.jwtAuthFilter            = jwtAuthFilter;
        this.authenticationProvider   = authenticationProvider;
        this.jwtProperties            = jwtProperties;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.rateLimitingFilter       = rateLimitingFilter;
        this.botDetectionFilter       = botDetectionFilter;
        this.ipExtractor              = ipExtractor;
        this.geoLocationService       = geoLocationService;
        this.objectMapper             = objectMapper;
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
            "X-RateLimit-Limit",
            "X-RateLimit-Remaining",
            "X-RateLimit-Reset"
        ));

        
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    @Bean
    @Order(1)
    public SecurityFilterChain publicFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher(new OrRequestMatcher(
                PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.GET,  "/support/faqs"),
                PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.GET,  "/support/articles/**"),
                PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/support/chat")
            ))
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .headers(headers -> headers.defaultsDisabled().cacheControl(Customizer.withDefaults()))
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
            .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(
                new RequestAuditFilter(ipExtractor, geoLocationService, objectMapper),
                jwtAuthFilter.getClass()
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers(
                    "/auth/**",
                    "/api/v1/auth/**",
                    "/error/**"
                ).permitAll()
                .requestMatchers("/home/**").permitAll()
                .requestMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/swagger-resources/**",
                    "/webjars/**",
                    "/docs",
                    "/docs/**",
                    "/v1/health",
                    "/webhook/**"
                ).permitAll() 
                .requestMatchers("/static/**").permitAll()
                .requestMatchers("/uploads/images/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/support/chat").permitAll()
                .requestMatchers(HttpMethod.GET,  "/support/articles/**").permitAll()
                .requestMatchers(HttpMethod.GET,  "/support/faqs").permitAll()
                .requestMatchers(HttpMethod.POST, "/support/tickets/**").hasRole("USER")
                .requestMatchers(HttpMethod.GET,  "/support/tickets/**").hasRole("USER")
                .requestMatchers(HttpMethod.POST, "/user/{id}/block").hasAnyRole("ADMIN", "SUPER_USER", "CUSTOMER_SERVICE")
                .requestMatchers(HttpMethod.POST, "/user/{id}/lock").hasAnyRole("ADMIN", "SUPER_USER")
                .requestMatchers(HttpMethod.DELETE, "/user/{id}").hasRole("SUPER_USER")
                .requestMatchers(HttpMethod.GET, "/user/{id}/edit").hasAnyRole("ADMIN", "SUPER_USER")
                .requestMatchers(HttpMethod.GET, "/user/{id}/view").hasAnyRole("ADMIN", "SUPER_USER")
                .requestMatchers("/admin/super/**").hasRole("SUPER_USER")
                .requestMatchers(HttpMethod.GET,    "/admin/users/**").hasAnyRole("ADMIN", "SUPER_USER", "CUSTOMER_SERVICE")
                .requestMatchers(HttpMethod.POST,   "/admin/users").hasAnyRole("ADMIN", "SUPER_USER")
                .requestMatchers(HttpMethod.PUT,    "/admin/users/**").hasAnyRole("ADMIN", "SUPER_USER")
                .requestMatchers(HttpMethod.DELETE, "/admin/users/**").hasRole("SUPER_USER")
                .requestMatchers("/admin/users/**").hasAnyRole("ADMIN", "SUPER_USER", "CUSTOMER_SERVICE")
                .requestMatchers(HttpMethod.GET,    "/admin/wallets/**").hasAnyRole("ADMIN", "SUPER_USER", "CUSTOMER_SERVICE")
                .requestMatchers("/admin/wallets/**").hasAnyRole("ADMIN", "SUPER_USER", "CUSTOMER_SERVICE")
                .requestMatchers(HttpMethod.GET,    "/admin/transactions/**").hasAnyRole("ADMIN", "SUPER_USER", "CUSTOMER_SERVICE")
                .requestMatchers(HttpMethod.PATCH,  "/admin/transactions/**").hasAnyRole("ADMIN", "SUPER_USER", "CUSTOMER_SERVICE")
                .requestMatchers("/admin/transactions/**").hasAnyRole("ADMIN", "SUPER_USER", "CUSTOMER_SERVICE")
                .requestMatchers(HttpMethod.GET,    "/admin/tickets/**").hasAnyRole("ADMIN", "SUPER_USER", "CUSTOMER_SERVICE", "EDITOR")
                .requestMatchers(HttpMethod.DELETE, "/admin/tickets/**").hasAnyRole("ADMIN", "SUPER_USER")
                .requestMatchers("/admin/tickets/**").hasAnyRole("ADMIN", "SUPER_USER", "CUSTOMER_SERVICE")
                .requestMatchers("/admin/**").hasAnyRole("ADMIN", "SUPER_USER")
                .requestMatchers("/tickets/**").hasRole("USER")
                .requestMatchers("/beneficiaries/**").hasRole("USER")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .bearerTokenResolver(request -> {
                    String header = request.getHeader("Authorization");
                    if (header != null && header.startsWith("Bearer ")) {
                        return header.substring(7).trim();
                    }
                    return null;
                })
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                    .decoder(jwtDecoder())
                )
                .authenticationEntryPoint(authenticationEntryPoint)
            )
            .authenticationProvider(authenticationProvider)
            .exceptionHandling(handling -> handling
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(customAccessDeniedHandler())
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
            
        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
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

        firewall.setAllowSemicolon(false);
        firewall.setAllowUrlEncodedPercent(false);
        firewall.setAllowBackSlash(false);
        firewall.setAllowUrlEncodedSlash(false);
        firewall.setAllowUrlEncodedPeriod(true);
        firewall.setAllowUrlEncodedDoubleSlash(false);
        firewall.setAllowNull(false);
        firewall.setAllowUrlEncodedLineFeed(false);
        firewall.setAllowUrlEncodedCarriageReturn(false);
        firewall.setAllowUrlEncodedParagraphSeparator(false);
        firewall.setAllowedHttpMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"
        ));
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
            com.epay.common.exception.ErrorResponse body =
                    com.epay.common.exception.ErrorResponse.builder()
                    .success(false)
                    .errorId(java.util.UUID.randomUUID().toString())
                    .errorCode(com.epay.common.exception.ErrorCode.FORBIDDEN_ACCESS)
                    .message("You do not have permission to access this resource")
                    .timestamp(java.time.Instant.now())
                    .path(request.getRequestURI())
                    .status(HttpServletResponse.SC_FORBIDDEN)
                    .build();
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            new com.fasterxml.jackson.databind.ObjectMapper()
                    .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
                    .writeValue(response.getWriter(), body);
        };
    }
}