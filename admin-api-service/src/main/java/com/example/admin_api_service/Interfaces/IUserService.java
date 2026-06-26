package com.example.admin_api_service.Interfaces;

import com.example.admin_api_service.dto.AdminDTO;
import com.example.admin_api_service.models.AdminUser;

public interface IUserService {
    boolean isUserRegistered(String username);
    
    boolean isUserExists(String username);

    boolean isUserEmailExists(String email);
    
    AdminUser getUserByUsername(String username);

    AdminDTO findPublicUserByUsername(String username);
}
