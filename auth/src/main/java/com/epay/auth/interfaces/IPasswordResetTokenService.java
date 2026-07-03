package com.epay.auth.interfaces;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import com.epay.domain.auth.input.ChangePasswordRequest;

public interface IPasswordResetTokenService {
    void createUserUserSession(Long userId, String token);

    ResponseEntity<?> findByToken(String token);

    boolean updatePassword(ChangePasswordRequest request);

    ResponseEntity<?> resetPassword(ChangePasswordRequest request, Authentication authentication);
}
