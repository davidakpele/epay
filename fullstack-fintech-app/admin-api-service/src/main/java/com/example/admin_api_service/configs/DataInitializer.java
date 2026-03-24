package com.example.admin_api_service.configs;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.example.admin_api_service.models.AdminUser;
import com.example.admin_api_service.models.accessAndSecurity.AdminRole;
import com.example.admin_api_service.repository.AdminRoleRepository;
import com.example.admin_api_service.repository.AdminUserRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@Profile("!test")
@RequiredArgsConstructor
public class DataInitializer {

    private final AdminUserRepository userRepository;
    private final AdminRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initData() {
        return args -> seedUsers();
    }

    @Transactional
    public void seedUsers() {

        // ─── Seed Roles ───────────────────────────────────────────────────────

        AdminRole adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> {
                    AdminRole r = new AdminRole();
                    r.setName("ADMIN");
                    r.setDescription("Administrator with full access");
                    r.setSystem(true);
                    r.setActive(true);
                    return roleRepository.save(r);
                });

        AdminRole managerRole = roleRepository.findByName("MANAGER")
                .orElseGet(() -> {
                    AdminRole r = new AdminRole();
                    r.setName("MANAGER");
                    r.setDescription("Manager with limited access");
                    r.setSystem(true);
                    r.setActive(true);
                    return roleRepository.save(r);
                });

        // ─── Seed Admin User ──────────────────────────────────────────────────

        if (userRepository.findByUsername("admin").isEmpty()) {
            AdminUser admin = AdminUser.builder()
                    .username("admin")
                    .email("admin@payrix.com")
                    .password(passwordEncoder.encode("admin123"))
                    .firstName("Admin")
                    .lastName("User")
                    .gender("Male")
                    .role(adminRole)
                    .active(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .notificationEnabled(true)
                    .build();

            userRepository.save(admin);
            System.out.println("✓ Admin user created");
            System.out.println("  Username  : " + admin.getUsername());
            System.out.println("  Email     : " + admin.getEmail());
            System.out.println("  Full Name : " + admin.getFirstName() + " " + admin.getLastName());
            System.out.println("  Gender    : " + admin.getGender());
            System.out.println("  Role      : " + adminRole.getName());
        } else {
            System.out.println("✓ Admin user already exists");
        }

        // ─── Seed Manager User ────────────────────────────────────────────────

        if (userRepository.findByUsername("manager").isEmpty()) {
            AdminUser manager = AdminUser.builder()
                    .username("manager")
                    .email("manager@payrix.com")
                    .password(passwordEncoder.encode("manager123"))
                    .firstName("Jane")
                    .lastName("Doe")
                    .gender("Female")
                    .role(managerRole)
                    .active(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .notificationEnabled(true)
                    .build();

            userRepository.save(manager);
            System.out.println("✓ Manager user created");
            System.out.println("  Username  : " + manager.getUsername());
            System.out.println("  Email     : " + manager.getEmail());
            System.out.println("  Full Name : " + manager.getFirstName() + " " + manager.getLastName());
            System.out.println("  Gender    : " + manager.getGender());
            System.out.println("  Role      : " + managerRole.getName());
        } else {
            System.out.println("✓ Manager user already exists");
        }

        System.out.println("=== Database Initialization Complete ===");
    }
}