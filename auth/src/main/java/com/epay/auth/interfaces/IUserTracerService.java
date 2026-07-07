package com.epay.auth.interfaces;

import com.epay.domain.auth.entity.User;
import com.epay.domain.auth.entity.UserTracer;

public interface IUserTracerService {

    boolean hasActiveSession(Long userId);
    
    UserTracer createSession(User user);

    void deleteSession(String sessionId);

    void deleteByUserId(Long userId);

    boolean isSessionValid(String sessionId);
}
