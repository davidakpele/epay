package com.payrix.administrator.services;

import com.payrix.administrator.models.User;
import com.payrix.administrator.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.logging.Logger;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private static final Logger logger = Logger.getLogger(CustomUserDetailsService.class.getName());

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("🔐 Loading user by username: " + username);
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> {
                logger.warning("❌ User not found with username: " + username);
                return new UsernameNotFoundException("User not found with username: " + username);
            });

        logger.info("✅ Found user: " + user.getUsername() + 
                   " | Role: " + user.getRole() + 
                   " | Enabled: " + user.isEnabled() +
                   " | Authorities: " + user.getAuthorities());

        // Since User implements UserDetails, we can return it directly
        return user;
    }
}