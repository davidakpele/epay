package com.example.administrator_api.configurations;

import java.time.LocalDateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.administrator_api.enums.Role;
import com.example.administrator_api.models.User;
import com.example.administrator_api.repository.UserRepository;
import reactor.core.publisher.Mono;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            System.out.println("=== Initializing Database ===");
            userRepository.findByUsername("admin")
                .flatMap(existingUser -> {
                    System.out.println("✓ Admin user already exists");
                    return Mono.just(existingUser);
                })
                .switchIfEmpty(
                    // Create admin if not found
                    Mono.defer(() -> {
                        User admin = User.create(
                            "admin",
                            "admin@payrix.com",
                            passwordEncoder.encode("admin123"),
                            "Admin",
                            "User",
                            Role.ADMIN 
                        );
                        admin.setCreatedAt(LocalDateTime.now());
                        admin.setUpdatedAt(LocalDateTime.now());
                        
                        System.out.println("✓ Admin user created: admin / admin123");
                        System.out.println("  Role: " + admin.getRole());
                        System.out.println("  Enabled: " + admin.isEnabled());
                        
                        return userRepository.save(admin);
                    })
                )
                .subscribe(); 
        };
    }
}