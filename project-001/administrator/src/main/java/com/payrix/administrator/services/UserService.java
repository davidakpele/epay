package com.payrix.administrator.services;

import org.springframework.stereotype.Service;
import com.payrix.administrator.dtos.AdminDTO;
import com.payrix.administrator.models.User;
import com.payrix.administrator.repositories.UserRepository;

@Service 
public class UserService {

    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
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

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public AdminDTO findPublicUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(AdminDTO::fromEntity)
                .orElse(null);
    }


    
   
    
}
