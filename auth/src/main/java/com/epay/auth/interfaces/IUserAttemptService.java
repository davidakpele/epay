package com.epay.auth.interfaces;

import org.springframework.http.ResponseEntity;
import com.epay.domain.auth.enums.AttemptType;

public interface IUserAttemptService {
    ResponseEntity<?> createFailAttempt(Long id, AttemptType login);

    ResponseEntity<?> UpdateUserAccount(Long id);
    
    ResponseEntity<?> deleteAttempt(Long id);
}
