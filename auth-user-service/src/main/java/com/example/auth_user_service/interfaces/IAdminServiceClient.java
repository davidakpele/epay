package com.example.auth_user_service.interfaces;

public interface IAdminServiceClient {
    boolean verifyUser(String username, String token);
    
}
