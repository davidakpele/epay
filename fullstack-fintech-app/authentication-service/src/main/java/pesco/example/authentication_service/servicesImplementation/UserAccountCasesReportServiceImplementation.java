package pesco.example.authentication_service.servicesImplementation;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import pesco.example.authentication_service.enums.ReportCasesAction;
import pesco.example.authentication_service.enums.UserStatus;
import pesco.example.authentication_service.models.UserAccountCases;
import pesco.example.authentication_service.models.UserRecord;
import pesco.example.authentication_service.models.Users;
import pesco.example.authentication_service.payloads.DeleteteAccountRequest;
import pesco.example.authentication_service.repositories.UserAccountCasesReportRepository;
import pesco.example.authentication_service.repositories.UserRecordRepository;
import pesco.example.authentication_service.repositories.UsersRepository;
import pesco.example.authentication_service.services.UserAccountCasesReportService;

@Service
public class UserAccountCasesReportServiceImplementation implements UserAccountCasesReportService {

    private final UserAccountCasesReportRepository userAccountCasesReportRepository;
    private final UsersRepository usersRepository;
    private final UserRecordRepository userRecordRepository;
    
    public UserAccountCasesReportServiceImplementation(UserAccountCasesReportRepository userAccountCasesReportRepository, UsersRepository usersRepository, UserRecordRepository userRecordRepository) {
        this.userAccountCasesReportRepository = userAccountCasesReportRepository;
        this.usersRepository = usersRepository;
        this.userRecordRepository = userRecordRepository;
    }

    @Override
    public ResponseEntity<?> deleteUserAccount(Long id, DeleteteAccountRequest deleteAccountRequest) {
        Users user = usersRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body("User with ID " + id + " does not exist");
        }

        UserAccountCases reportCase = userAccountCasesReportRepository.findByUserId(id);
        if (reportCase != null) {
            return ResponseEntity.badRequest().body("Report case to delete this account has already exists for user with ID " + id);
        }else {
            reportCase = new UserAccountCases();
        }
        
        if (!Arrays.stream(ReportCasesAction.values()).anyMatch(action -> action.name().equalsIgnoreCase(deleteAccountRequest.getAction()))) {
            return ResponseEntity.badRequest().body("Invalid action provided");
        }
        
        // Covert action to enum
        ReportCasesAction actionEnum = ReportCasesAction.valueOf(deleteAccountRequest.getAction());

        reportCase.setReasons(deleteAccountRequest.getReason());
        reportCase.setUsername(deleteAccountRequest.getUsername());
        reportCase.setUserId(deleteAccountRequest.getUserId());
        reportCase.setEmail(deleteAccountRequest.getEmail());
        reportCase.setAction(actionEnum);
        reportCase.setCreatedOn(LocalDateTime.now());
        userAccountCasesReportRepository.save(reportCase); 
        
        user.setEnabled(false);
        usersRepository.save(user);

        UserRecord userRecord = userRecordRepository.findByUserId(user.getId())
            .orElseThrow(() -> new RuntimeException("User record not found"));

        userRecord.setLocked(true);
        userRecord.setLockedAt(LocalDateTime.now());
        userRecord.setBlocked(true);
        userRecord.setBlockedReason(deleteAccountRequest.getReason());
        userRecord.setStatus(UserStatus.SUSPENDED); 
        userRecordRepository.save(userRecord);
        
        return ResponseEntity.ok(
            Map.of(
                "status", "success",
                "message", "User account has been blocked and locked waiting for admin review."
            )
    );
    }

    
}
