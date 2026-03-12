package com.example.administrator_api.security;

import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.example.administrator_api.repository.UserRepository;
import reactor.core.publisher.Mono;
import java.util.logging.Logger;

@Service
public class CustomUserDetailsService implements ReactiveUserDetailsService {

    private final UserRepository userRepository;
    private static final Logger logger = Logger.getLogger(CustomUserDetailsService.class.getName());

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        logger.info("Loading user by username: " + username);

        return userRepository.findByUsername(username)
            .switchIfEmpty(Mono.defer(() -> {
                logger.warning("User not found with username: " + username);
                return Mono.error(new UsernameNotFoundException(
                    "User not found with username: " + username));
            }))
            .doOnNext(user -> logger.info(
                "Found user: " + user.getUsername() +
                " | Role: " + user.getRole() +
                " | Enabled: " + user.isEnabled() +
                " | Authorities: " + user.getAuthorities()
            ))
            .cast(UserDetails.class);
    }
}