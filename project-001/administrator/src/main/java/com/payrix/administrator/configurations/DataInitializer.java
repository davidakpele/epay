package com.payrix.administrator.configurations;

import java.time.LocalDateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.payrix.administrator.enums.Role;
import com.payrix.administrator.models.User;
import com.payrix.administrator.repositories.UserRepository;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            System.out.println("=== Initializing Database ===");
            
            // Create admin user if not exists
            if (userRepository.findByUsername("admin").isEmpty()) {
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
                
                userRepository.save(admin);
                System.out.println("✓ Admin user created: admin / admin123");
                System.out.println("  Role: " + admin.getRole());
                System.out.println("  Enabled: " + admin.isEnabled());
            } else {
                System.out.println("✓ Admin user already exists");
            }
          
        };
    }
}