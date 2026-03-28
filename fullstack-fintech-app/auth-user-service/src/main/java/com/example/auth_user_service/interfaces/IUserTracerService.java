package com.example.auth_user_service.interfaces;

import com.example.auth_user_service.models.UserTracer;
import com.example.auth_user_service.models.Users;

public interface IUserTracerService {

    boolean hasActiveSession(Long userId);
    
    UserTracer createSession(Users user);

    void deleteSession(String sessionId);

    void deleteByUserId(Long userId);

    boolean isSessionValid(String sessionId);
}
