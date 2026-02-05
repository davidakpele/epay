package pesco.example.authentication_service.servicesImplementation;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import pesco.example.authentication_service.enums.AttemptType;
import pesco.example.authentication_service.models.UserAttempt;
import pesco.example.authentication_service.models.UserRecord;
import pesco.example.authentication_service.repositories.UserAttemptRepository;
import pesco.example.authentication_service.repositories.UserRecordRepository;
import pesco.example.authentication_service.services.UserAttemptService;


@Service
public class UserAttemptServiceImplementations implements UserAttemptService {

    private final UserAttemptRepository userAttemptRepository;
    private final UserRecordRepository userRecordRepository;

    public UserAttemptServiceImplementations(UserAttemptRepository userAttemptRepository, UserRecordRepository userRecordRepository) {
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

}
