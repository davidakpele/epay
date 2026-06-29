package com.example.auth_user_service.services;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.auth_user_service.components.NotificationProperties;
import com.example.auth_user_service.exceptions.Error;
import com.example.auth_user_service.httpClients.NotificationServiceClient;
import com.example.auth_user_service.interfaces.IPasswordResetTokenService;
import com.example.auth_user_service.models.PasswordResetToken;
import com.example.auth_user_service.models.UserRecord;
import com.example.auth_user_service.models.Users;
import com.example.auth_user_service.payloads.ChangePasswordRequest;
import com.example.auth_user_service.repositories.PasswordResetTokenRepository;
import com.example.auth_user_service.repositories.UserRecordRepository;
import com.example.auth_user_service.repositories.UsersRepository;

@Service
public class PasswordResetTokenService implements IPasswordResetTokenService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsersRepository userRepository;
    private final UserRecordRepository userRecordRepository;
    private final NotificationServiceClient notificationServiceClient;
    private final NotificationProperties notificationProperties;

    private static final DateTimeFormatter EVT_FMT =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy hh:mm:ss a");

    private String formatNow() {
        return ZonedDateTime.now(ZoneId.systemDefault()).format(EVT_FMT);
    }

    public PasswordResetTokenService(PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordEncoder passwordEncoder,
            UsersRepository userRepository,
            UserRecordRepository userRecordRepository,
            NotificationServiceClient notificationServiceClient,
            NotificationProperties notificationProperties) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder              = passwordEncoder;
        this.userRepository               = userRepository;
        this.userRecordRepository         = userRecordRepository;
        this.notificationServiceClient    = notificationServiceClient;
        this.notificationProperties       = notificationProperties;
    }

    @Override
    public ResponseEntity<?> findByToken(String token) {
        PasswordResetToken passOptional = passwordResetTokenRepository.findByToken(token);
        if (passOptional == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token");
        }
        if (passOptional.isExpired()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Expired token");
        }
        return ResponseEntity.ok("valid");
    }

    @Override
    public boolean updatePassword(ChangePasswordRequest request) {
        PasswordResetToken passOptional = passwordResetTokenRepository.findByToken(request.getToken());

        if (passOptional != null && !passOptional.isExpired()) {
            Long userId = passOptional.getUserId();
            Optional<Users> userOptional = userRepository.findById(userId);

            if (userOptional.isPresent()) {
                Users user = userOptional.get();
                user.setPassword(passwordEncoder.encode(request.getPassword()));
                userRepository.save(user);

                // Delete the token
                passwordResetTokenRepository.delete(passOptional);
                return true;
            }
        }

        return false;
    }

    @Override
    public ResponseEntity<?> resetPassword(ChangePasswordRequest request, Authentication authentication) {
        String username = authentication.getName();
        Optional<Users> optionUser = userRepository.findByUsername(username);

        if (optionUser != null && optionUser.isPresent()) {
            Users user = optionUser.get();
            if (passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                user.setPassword(passwordEncoder.encode(request.getPassword()));
                userRepository.save(user);

                // ── Security notification ─────────────────────────────────────
                final String eventTime = formatNow();
                userRecordRepository.findByUserId(user.getId()).ifPresent(rec -> {
                    final String fullName = rec.getFirstName() + " " + rec.getLastName();
                    CompletableFuture.runAsync(() ->
                        notificationServiceClient.sendAccountSecurityAlert(
                            user.getEmail(), fullName, user.getUsername(),
                            "UPDATE_PASSWORD", eventTime, "", "",
                            notificationProperties.getPhone(), notificationProperties.getEmail()
                        )
                    ).exceptionally(ex -> { System.err.println("[UpdatePassword] " + ex.getMessage()); return null; });
                });
                // ─────────────────────────────────────────────────────────────

                return Error.createResponse("Password successfully updated.", HttpStatus.CREATED, "Success");
            } else {
                return Error.createResponse("Currect Password do not match system password*", HttpStatus.BAD_REQUEST,
                        "The old password does not match with the your password in the system.");
            }
        }
        return Error.createResponse("Sorry, something went wrong.", HttpStatus.BAD_REQUEST,
                "Sorry, something went wrong in processing update.");
    }

    @Override
    public void createUserUserSession(Long userId, String token) {
        Optional<PasswordResetToken> passOptional = passwordResetTokenRepository.findByUserId(userId);
        // Delete existing token if present
        passOptional.ifPresent(passwordResetTokenRepository::delete);

        // Create and save new token
        PasswordResetToken newToken = new PasswordResetToken(token, userId);
        passwordResetTokenRepository.save(newToken);
    }

}
