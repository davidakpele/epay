package com.example.auth_user_service.interfaces;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import com.example.auth_user_service.payloads.ChangePasswordRequest;

public interface IPasswordResetTokenService {
    void createUserUserSession(Long userId, String token);

    ResponseEntity<?> findByToken(String token);

    boolean updatePassword(ChangePasswordRequest request);

    ResponseEntity<?> resetPassword(ChangePasswordRequest request, Authentication authentication);
}
