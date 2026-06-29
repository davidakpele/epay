package com.example.auth_user_service.services;

import com.example.auth_user_service.components.KeyWrapper;
import com.example.auth_user_service.components.NotificationProperties;
import com.example.auth_user_service.dtos.PageResponse;
import com.example.auth_user_service.dtos.UserDTO;
import com.example.auth_user_service.httpClients.NotificationServiceClient;
import com.example.auth_user_service.interfaces.IUserService;
import com.example.auth_user_service.models.UserRecord;
import com.example.auth_user_service.models.Users;
import com.example.auth_user_service.repositories.UserRecordRepository;
import com.example.auth_user_service.repositories.UsersRepository;
import com.example.auth_user_service.responses.UserStatisticsResponse;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import com.example.auth_user_service.exceptions.ApiException;
import com.example.auth_user_service.exceptions.Error;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService implements IUserService {

    private final NotificationServiceClient notificationServiceClient;
    private final UsersRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenService passwordResetTokenService;
    private final KeyWrapper keysWrapper;
    private final UserRecordRepository userRecordRepository;
    private final NotificationProperties notificationProperties;

    private static final DateTimeFormatter EVT_FMT =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy hh:mm:ss a");

    private String formatNow() {
        return ZonedDateTime.now(ZoneId.systemDefault()).format(EVT_FMT);
    }

    public UserService(NotificationServiceClient notificationServiceClient,
                       UsersRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       PasswordResetTokenService passwordResetTokenService,
                       KeyWrapper keysWrapper,
                       UserRecordRepository userRecordRepository,
                       NotificationProperties notificationProperties) {
        this.notificationServiceClient = notificationServiceClient;
        this.userRepository            = userRepository;
        this.passwordEncoder           = passwordEncoder;
        this.passwordResetTokenService = passwordResetTokenService;
        this.keysWrapper               = keysWrapper;
        this.userRecordRepository      = userRecordRepository;
        this.notificationProperties    = notificationProperties;
    }
    
    @Override
    public Users getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    @Override
    public Users getUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }
    
    @Override
    public Optional<Users> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public ResponseEntity<?> resetPassword(Long userId, String password, Authentication authentication) {
        Users user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request: User not found");
        }

        String authenticatedUsername = authentication.getName();
        if (!user.getUsername().equals(authenticatedUsername)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid request: Unauthorized to reset password");
        }

        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);

        // ── Security notification ─────────────────────────────────────────────
        final String eventTime = formatNow();
        userRecordRepository.findByUserId(userId).ifPresent(rec -> {
            final String fullName = rec.getFirstName() + " " + rec.getLastName();
            CompletableFuture.runAsync(() ->
                notificationServiceClient.sendAccountSecurityAlert(
                    user.getEmail(), fullName, user.getUsername(),
                    "PASSWORD_RESET", eventTime, "", "",
                    notificationProperties.getPhone(), notificationProperties.getEmail()
                )
            ).exceptionally(ex -> { System.err.println("[PasswordReset] " + ex.getMessage()); return null; });
        });
        // ─────────────────────────────────────────────────────────────────────

        return ResponseEntity.ok("Password reset successful");
    }

    @Override
    public Object deactivateAccount(Long id, Authentication authentication) {
        Users user = userRepository.findById(id).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request: User not found");
        }

        String authenticatedUsername = authentication.getName();
        if (!user.getUsername().equals(authenticatedUsername)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid request: Unauthorized to reset password");
        }
        user.setEnabled(false);
        userRepository.save(user);

        Optional<UserRecord> recordOptional = userRecordRepository.findByUserId(id);
        if (recordOptional.isPresent()) {
            UserRecord record = recordOptional.get();
            record.setLocked(true);
            record.setBlocked(true);
            userRecordRepository.save(record);
        } else {
            System.out.println("Nothing found");
        }

        // ── Security notification ─────────────────────────────────────────────
        final String eventTime = formatNow();
        userRecordRepository.findByUserId(id).ifPresent(rec -> {
            final String fullName = rec.getFirstName() + " " + rec.getLastName();
            CompletableFuture.runAsync(() ->
                notificationServiceClient.sendAccountSecurityAlert(
                    user.getEmail(), fullName, user.getUsername(),
                    "DEACTIVATE_ACCOUNT", eventTime, "", "",
                    notificationProperties.getPhone(), notificationProperties.getEmail()
                )
            ).exceptionally(ex -> { System.err.println("[DeactivateAccount] " + ex.getMessage()); return null; });
        });
        // ─────────────────────────────────────────────────────────────────────

        return ResponseEntity.ok("Account deactivated successfully");
    }

    @Override
    public ResponseEntity<?> forgetPassword(String email) {
        Map<String, Object> response = new HashMap<>();
        Optional<Users> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            String token = UUID.randomUUID().toString();

            passwordResetTokenService.createUserUserSession(user.get().getId(), token);
            // Send email
            String url = keysWrapper.getUrl() + "/auth/reset-password?token=" + token;
            String content = "We received a request to reset the password for your account associated with this email address. If you did not request this change, please ignore this email."
                    + "To reset your password, please click on the link below:";
       
            CompletableFuture<Void>sendResetPasswordMessage = CompletableFuture.runAsync(() -> notificationServiceClient.sendPasswordResetMessage(user.get().getEmail(), user.get().getUsername(), content, url));
            sendResetPasswordMessage.join();
        
            response.put("message",
                    "Message has been sent to the very email address provided. Please follow the instructions to reset your password.");
            response.put("token", token);
            response.put("status", HttpStatus.OK);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        response.put("message", "User not found.!");
        response.put("status", HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @Override
    public ResponseEntity<?> updateUserProfile(String username, String email, String gender, String profilePath) {
        Optional<Users> optionalUser = userRepository.findByUsername(username);
        Optional<UserRecord> optionalUserRecord = userRecordRepository.findByUserId(optionalUser.get().getId());

        if (!optionalUser.isPresent()) {
            return Error.createResponse(
                    "UNAUTHORIZE ACCESS", HttpStatus.FORBIDDEN,
                    "You dont have access to the endpoints");
        }
        Users user = optionalUser.get();
        user.setUsername(username);
        user.setEmail(email);

        UserRecord record = optionalUserRecord.get();

        if (profilePath != null && !"".equals(profilePath)) {
            record.setPhoto(profilePath);
        }
        record.setGender(gender);
        userRepository.save(user);

        userRecordRepository.save(record);

        return ResponseEntity.ok().body("User updated successfully");
    }

    @Override
    public PageResponse<UserDTO> getAllUsersPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Users> usersPage = userRepository.findAll(pageable);
        List<UserDTO> content = usersPage.getContent()
                .stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
        return new PageResponse<>(
                content,
                page,
                size,
                usersPage.getTotalElements(),
                usersPage.getTotalPages(),
                usersPage.isLast(),
                usersPage.isFirst()
        );
    }

    @Override
    public Long countAllUsers() {
        return userRepository.count();
    }

    @Override
    public UserDTO findUserWithRecordById(Long id) {
        Users user = userRepository.findUserWithRecordById(id);
        if (user == null) return null;
        return UserDTO.fromEntity(user); 
    }

    @Override
    @Transactional
    public void deleteUserAccount(String id) {
        Long userId = Long.valueOf(id);

        
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("USER_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND));

        userRepository.delete(user);
    }

    @Override
    @Transactional
    public void lockUserAccount(Long id, boolean lock) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException("USER_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND));

        UserRecord record = user.getRecords().stream()
                .findFirst()
                .orElseThrow(() -> new ApiException("USER_RECORD_NOT_FOUND", "User record not found", HttpStatus.NOT_FOUND));

        record.setLocked(lock);
        record.setLockedAt(lock ? LocalDateTime.now() : null);
        userRecordRepository.save(record);

        // ── Security notification ─────────────────────────────────────────────
        final String eventTime = formatNow();
        final String eventType = lock ? "ACCOUNT_LOCKED" : "ACCOUNT_UNLOCKED";
        final String fullName  = record.getFirstName() + " " + record.getLastName();
        CompletableFuture.runAsync(() ->
            notificationServiceClient.sendAccountSecurityAlert(
                user.getEmail(), fullName, user.getUsername(),
                eventType, eventTime, "", "",
                notificationProperties.getPhone(), notificationProperties.getEmail()
            )
        ).exceptionally(ex -> { System.err.println("[LockAccount] " + ex.getMessage()); return null; });
        // ─────────────────────────────────────────────────────────────────────
    }

    @Override
    @Transactional
    public void blockUserAccount(Long id, boolean block) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException("USER_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND));

        UserRecord record = user.getRecords().stream()
                .findFirst()
                .orElseThrow(() -> new ApiException("USER_RECORD_NOT_FOUND", "User record not found", HttpStatus.NOT_FOUND));

        record.setBlocked(block);
        record.setBlockedReason(block ? "Admin block" : null);
        record.setBlockedUntil(block ? LocalDateTime.now().plusDays(7).toString() : null);
        userRecordRepository.save(record);

        final String eventTime = formatNow();
        final String eventType = block ? "ACCOUNT_BLOCKED" : "ACCOUNT_UNBLOCKED";
        final String fullName  = record.getFirstName() + " " + record.getLastName();
        CompletableFuture.runAsync(() ->
            notificationServiceClient.sendAccountSecurityAlert(
                user.getEmail(), fullName, user.getUsername(),
                eventType, eventTime, "", "",
                notificationProperties.getPhone(), notificationProperties.getEmail()
            )
        ).exceptionally(ex -> { System.err.println("[BlockAccount] " + ex.getMessage()); return null; });
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserDetails(Long id) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException("USER_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND));

        return UserDTO.fromEntity(user);
    }

    @Transactional(readOnly = true)
    @Override
    public UserStatisticsResponse getUserStatistics(String period) {

        long totalUsers    = userRepository.countAllRegularUsers();
        long activeUsers   = userRepository.countActiveUsers();
        long inactiveUsers = userRepository.countInactiveUsers();
        long kycPending    = userRepository.countKycPending();

        List<UserStatisticsResponse.AnalyticsDataPoint> trend =
                buildRegistrationTrend(period.toUpperCase());

        return UserStatisticsResponse.of(
                totalUsers,
                activeUsers,
                inactiveUsers,
                kycPending,
                trend,
                period.toUpperCase()
        );
    }

    private List<UserStatisticsResponse.AnalyticsDataPoint> buildRegistrationTrend(String period) {
        return switch (period) {

            case "DAILY" -> {
                LocalDateTime since = LocalDateTime.now().minusDays(30);
                yield userRepository.countDailyRegistrations(since).stream()
                        .map(row -> new UserStatisticsResponse.AnalyticsDataPoint(
                                row[0].toString(),
                                ((Number) row[1]).longValue()))
                        .toList();
            }

            case "WEEKLY" -> {
                LocalDateTime since = LocalDateTime.now().minusWeeks(12);
                yield userRepository.countWeeklyRegistrations(since).stream()
                        .map(row -> new UserStatisticsResponse.AnalyticsDataPoint(
                                "Week " + row[1] + " " + row[0],
                                ((Number) row[2]).longValue()))
                        .toList();
            }

            case "MONTHLY" -> {
                LocalDateTime since = LocalDateTime.now().minusMonths(12);
                yield userRepository.countMonthlyRegistrations(since).stream()
                        .map(row -> new UserStatisticsResponse.AnalyticsDataPoint(
                                row[0] + "-" + String.format("%02d", ((Number) row[1]).intValue()),
                                ((Number) row[2]).longValue()))
                        .toList();
            }

            case "YEARLY" ->
                userRepository.countYearlyRegistrations().stream()
                        .map(row -> new UserStatisticsResponse.AnalyticsDataPoint(
                                row[0].toString(),
                                ((Number) row[1]).longValue()))
                        .toList();

            default -> throw new IllegalArgumentException(
                    "Invalid period: " + period + ". Use DAILY, WEEKLY, MONTHLY or YEARLY");
        };
    }

}
