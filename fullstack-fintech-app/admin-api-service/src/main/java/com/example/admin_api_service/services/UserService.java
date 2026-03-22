package com.example.admin_api_service.services;

import org.springframework.stereotype.Service;

import com.example.admin_api_service.Interfaces.IUserService;
import com.example.admin_api_service.dto.AdminDTO;
import com.example.admin_api_service.models.AdminUser;
import com.example.admin_api_service.repository.AdminUserRepository;

@Service 
public class UserService implements IUserService{

    private final AdminUserRepository userRepository;
    
    public UserService(AdminUserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public boolean isUserRegistered(String username) {
        return userRepository.findByUsername(username) != null;
    }
    
    public boolean isUserExists(String username) {
        return userRepository.existsByUsername(username);
    }
    
    public boolean isUserEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public AdminUser getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public AdminDTO findPublicUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(AdminDTO::fromEntity)
                .orElse(null);
    }

}
