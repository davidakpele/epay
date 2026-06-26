package com.example.auth_user_service.interfaces;

import org.springframework.http.ResponseEntity;

import com.example.auth_user_service.enums.AttemptType;

public interface IUserAttemptService {

    ResponseEntity<?> createFailAttempt(Long id, AttemptType login);

    ResponseEntity<?> UpdateUserAccount(Long id);
    
    ResponseEntity<?> deleteAttempt(Long id);
}
