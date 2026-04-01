package com.example.auth_user_service.services;

import java.time.Instant;

import org.springframework.http.ResponseEntity;
import java.sql.Timestamp;
import java.util.Optional;
import org.springframework.stereotype.Service;
import com.example.auth_user_service.enums.AttemptType;
import com.example.auth_user_service.interfaces.IUserAttemptService;
import com.example.auth_user_service.models.UserAttempt;
import com.example.auth_user_service.models.UserRecord;
import com.example.auth_user_service.repositories.UserAttemptRepository;
import com.example.auth_user_service.repositories.UserRecordRepository;


@Service
public class UserAttemptService implements IUserAttemptService{
    private final UserAttemptRepository userAttemptRepository;
    private final UserRecordRepository userRecordRepository;

    public UserAttemptService(UserAttemptRepository userAttemptRepository, UserRecordRepository userRecordRepository) {
        this.userAttemptRepository = userAttemptRepository;
        this.userRecordRepository = userRecordRepository;
    }
    
    @Override
    public ResponseEntity<?> createFailAttempt(Long id, AttemptType login) {
         
        if (login == AttemptType.LOGIN){
            UserAttempt attempt = userAttemptRepository.findByUserId(id);
            if (attempt == null) {
                UserAttempt createNew = new UserAttempt();
                createNew.setUserId(id);
                createNew.setCounter(1);
                createNew.setSuccess(true);
                createNew.setAttemptType(login);
                createNew.setTimestamp(Timestamp.from(Instant.now()));

                userAttemptRepository.save(createNew);
            } 
            else {
                if (attempt.getCounter() >= 5) {
                    attempt.setSuccess(true);
                    userAttemptRepository.save(attempt);

                    Optional<UserRecord> user = userRecordRepository.findByUserId(id);
                    if (user.isPresent()) {
                        UserRecord updateStatus = user.get();
                        updateStatus.setLocked(true);

                        userRecordRepository.save(updateStatus);
                    }
                    return ResponseEntity.ok("This account has been locked due to too many attempts, Please contact our customer service.");
                } else if (attempt.getCounter() < 5) {
                    attempt.setCounter(attempt.getCounter() + 1);
                    userAttemptRepository.save(attempt);
                }
            }
        }
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<?> UpdateUserAccount(Long id) {
        UserAttempt attempt = userAttemptRepository.findByUserId(id);
        if (attempt != null) {
            userAttemptRepository.deleteById(attempt.getId());
            Optional<UserRecord> user = userRecordRepository.findByUserId(id);
            if (user.isPresent()) {
                UserRecord updateStatus = user.get();
                updateStatus.setLocked(false);
                userRecordRepository.save(updateStatus);
                return ResponseEntity.ok("Account has been successfully released.");
            }
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }

    @Override
    public ResponseEntity<?> deleteAttempt(Long id) {
       
        UserRecord user = userRecordRepository.findByUserId(id).orElse(null);
        user.setLocked(false);
        userRecordRepository.save(user);
        UserAttempt attempt = userAttemptRepository.findByUserId(id);
        if (attempt == null) {
            return ResponseEntity.notFound().build();
        }
        userAttemptRepository.deleteById(attempt.getId());

        return ResponseEntity.ok("User attempt record deleted successfully.");
    }

}
