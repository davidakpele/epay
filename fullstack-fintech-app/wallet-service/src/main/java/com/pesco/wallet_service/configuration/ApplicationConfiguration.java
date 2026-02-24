package com.pesco.wallet_service.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pesco.wallet_service.bootstrap.UsersDetailsDTO;
import com.pesco.wallet_service.client.UserServiceClient;
import com.pesco.wallet_service.dtos.UserRecordDTO;
import com.pesco.wallet_service.util.TokenExtractor;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.servlet.http.HttpServletRequest;

@Configuration
public class ApplicationConfiguration {

    private final UserServiceClient userServiceClient;
    private final TokenExtractor tokenExtractor;
    private final HttpServletRequest request;

    public ApplicationConfiguration(UserServiceClient userServiceClient,
                                    TokenExtractor tokenExtractor,
                                    HttpServletRequest request) {
        this.userServiceClient = userServiceClient;
        this.tokenExtractor = tokenExtractor;
        this.request = request;
    }
    
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            try {
                String token = tokenExtractor.extractToken(request);
                UsersDetailsDTO userDTO = userServiceClient.getUserByUsername(username, token);

                if (userDTO != null) {
                    List<SimpleGrantedAuthority> authorities = userDTO.getRecords().stream()
                            .map(record -> new SimpleGrantedAuthority("ROLE_USER"))
                            .toList();
                    return new org.springframework.security.core.userdetails.User(
                            userDTO.getUsername(),
                            "", 
                            userDTO.isEnabled(),
                            true, 
                            true, 
                            !isAccountLocked(userDTO), 
                            authorities
                    );
                } else {
                    throw new UsernameNotFoundException("User not found: " + username);
                }
            } catch (Exception e) {
                // Log the error and throw an exception to prevent unauthorized access
                throw new UsernameNotFoundException("Unable to fetch user details for: " + username, e);
            }
        };
    }

    // Helper method to determine if the account is locked
    private boolean isAccountLocked(UsersDetailsDTO userDTO) {
        return userDTO.getRecords().stream().anyMatch(UserRecordDTO::isLocked);
    }


    @SuppressWarnings("deprecation")
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
    
}
