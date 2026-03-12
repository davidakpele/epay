package com.example.administrator_api.configurations;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.example.administrator_api.security.CustomUserDetailsService;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfiguration(CustomUserDetailsService customUserDetailsService,
                                 JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
            "/admin/auth/login",
                        "/admin/auth/logout",
                        "/css/**",
                        "/js/**",
                        "/bower_components/**",
                        "/dist/**",
                        "/plugins/**",
                        "/fonts/**",
                        "/images/**",
                        "/img/**",
                        "/error",
                        "/admin/api/auth/login",
                        "/admin/api/auth/register",
                        "/admin/api/auth/refresh",
                        "/admin/api/auth/logout",
                        "/admin/api/verify-user",
                        "/admin/api/username/**"  
                    ).permitAll()
                    .requestMatchers("/admin/dashboard").hasRole("ADMIN")
                    .requestMatchers("/admin/users").hasRole("ADMIN")
                    .requestMatchers("/admin/user/**").hasAnyRole("USER", "ADMIN")
                    .requestMatchers("/admin/api/**").hasAnyRole("USER", "ADMIN")
                    .anyRequest().authenticated()
                )
                .formLogin(form -> form
                    .loginPage("/admin/auth/login")
                    .loginProcessingUrl("/admin/auth/login")
                    .defaultSuccessUrl("/admin/dashboard", true)
                    .failureUrl("/admin/auth/login?error=true")
                    .permitAll()
                )
                .logout(logout -> logout
                    .logoutUrl("/admin/auth/logout")
                    .logoutSuccessUrl("/admin/auth/login?logout=true")
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID", "jwt-token")
                    .permitAll()
                )
                
            .exceptionHandling(handling -> handling
                .accessDeniedHandler(customAccessDeniedHandler())
                .authenticationEntryPoint((request, response, authException) -> {
                    String requestUri = request.getRequestURI();
                    if (requestUri.startsWith("/api/")) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}");
                    } else {
                        response.sendRedirect("/admin/auth/login");
                    }
                })
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
            
        return http.build();
    }


    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public AccessDeniedHandler customAccessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            String requestUri = request.getRequestURI();
            if (requestUri.startsWith("/api/")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Forbidden\",\"message\":\"Access denied\"}");
            } else {
                response.sendRedirect("/admin/login?denied=true");
            }
        };
    }
}
