package com.example.auth_user_service.interfaces;

import org.springframework.http.ResponseEntity;

import com.example.auth_user_service.enums.AttemptType;

public interface UserAttemptService {

    ResponseEntity<?> createFailAttempt(Long id, AttemptType login);

    ResponseEntity<?> UpdateUserAccount(Long id);

}
