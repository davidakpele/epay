package pesco.example.virtual_card_service.configurations;

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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import pesco.example.virtual_card_service.bootstrap.UsersDetailsDTO;
import pesco.example.virtual_card_service.clients.UserServiceClient;
import pesco.example.virtual_card_service.components.TokenExtractor;
import pesco.example.virtual_card_service.dto.UserRecordDTO;

@Configuration
public class ApplicationConfiguration {

    private final UserServiceClient userServiceClient;
    private final TokenExtractor tokenExtractor;

    public ApplicationConfiguration(UserServiceClient userServiceClient,
                                    TokenExtractor tokenExtractor) {
        this.userServiceClient = userServiceClient;
        this.tokenExtractor = tokenExtractor;
    }
    
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            try {
                ServletRequestAttributes attributes = 
                    (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
                HttpServletRequest request = attributes.getRequest();
                
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
            } catch (IllegalStateException e) {
                throw new UsernameNotFoundException(
                    "Unable to fetch user details - no request context for: " + username, e);
            } catch (UsernameNotFoundException e) {
                throw new UsernameNotFoundException(
                    "Unable to fetch user details for: " + username, e);
            }
        };
    }

    private boolean isAccountLocked(UsersDetailsDTO userDTO) {
        return userDTO.getRecords().stream().anyMatch(UserRecordDTO::isLocked);
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService());
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