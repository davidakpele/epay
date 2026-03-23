// package com.example.admin_api_service.configs;

// import com.example.admin_api_service.enums.AdminRole;
// import com.example.admin_api_service.models.AdminUser;
// import com.example.admin_api_service.repository.AdminUserRepository;
// import lombok.RequiredArgsConstructor;
// import org.springframework.boot.CommandLineRunner;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.context.annotation.Profile;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.transaction.annotation.Transactional;

// import java.time.LocalDateTime;

// @Configuration
// @Profile("!test")
// @RequiredArgsConstructor
// public class DataInitializer {

//     private final AdminUserRepository userRepository;
//     private final PasswordEncoder passwordEncoder;

//     @Bean
//     public CommandLineRunner initData() {
//         return args -> seedUsers();
//     }

//     @Transactional
//     public void seedUsers() {
//         if (userRepository.findByUsername("admin").isEmpty()) {
//             AdminUser admin = AdminUser.builder()
//                     .username("admin")
//                     .email("admin@payrix.com")
//                     .password(passwordEncoder.encode("admin123"))
//                     .firstName("Admin")
//                     .lastName("User")
//                     .gender("Male")
//                     .role(AdminRole.ADMIN)
//                     .active(true)
//                     .accountNonExpired(true)
//                     .accountNonLocked(true)
//                     .credentialsNonExpired(true)
//                     .createdAt(LocalDateTime.now())
//                     .updatedAt(LocalDateTime.now())
//                     .build();

//             userRepository.save(admin);
//             System.out.println("✓ Admin user created");
//             System.out.println("  Username  : " + admin.getUsername());
//             System.out.println("  Email     : " + admin.getEmail());
//             System.out.println("  Full Name : " + admin.getFirstName() + " " + admin.getLastName());
//             System.out.println("  Gender    : " + admin.getGender());
//             System.out.println("  Role      : " + admin.getRole());
//             System.out.println("  Enabled   : " + admin.isEnabled());
//         } else {
//             System.out.println("✓ Admin user already exists");
//         }

//         if (userRepository.findByUsername("manager").isEmpty()) {
//             AdminUser manager = AdminUser.builder()
//                     .username("manager")
//                     .email("manager@payrix.com")
//                     .password(passwordEncoder.encode("manager123"))
//                     .firstName("Jane")
//                     .lastName("Doe")
//                     .gender("Female")
//                     .role(AdminRole.MANAGER)
//                     .active(true)
//                     .accountNonExpired(true)
//                     .accountNonLocked(true)
//                     .credentialsNonExpired(true)
//                     .createdAt(LocalDateTime.now())
//                     .updatedAt(LocalDateTime.now())
//                     .build();

//             userRepository.save(manager);
//             System.out.println("✓ Manager user created");
//             System.out.println("  Username  : " + manager.getUsername());
//             System.out.println("  Role      : " + manager.getRole());
//         } else {
//             System.out.println("✓ Manager user already exists");
//         }

//         System.out.println("=== Database Initialization Complete ===");
//     }
// }