package pesco.example.authentication_service.services;

import pesco.example.authentication_service.models.UserTracer;
import pesco.example.authentication_service.models.Users;

public interface UserTracerService {

    boolean hasActiveSession(Long userId);
    
    UserTracer createSession(Users user);

    void deleteSession(String sessionId);

    void deleteByUserId(Long userId);

    boolean isSessionValid(String sessionId);
}
