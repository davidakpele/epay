package com.example.admin_api_service.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.admin_api_service.models.AdminUser;
import com.example.admin_api_service.repository.AdminUserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AdminUserRepository adminUserRepository;

    public CustomUserDetailsService(AdminUserRepository adminUserRepository) {
        this.adminUserRepository = adminUserRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
   
        AdminUser user = adminUserRepository.findByUsername(username)
            .orElseThrow(() -> {
                return new UsernameNotFoundException("User not found with username: " + username);
            });
        return user;
    }
    
}
