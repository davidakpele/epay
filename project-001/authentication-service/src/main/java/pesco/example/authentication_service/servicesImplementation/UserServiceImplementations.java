package pesco.example.authentication_service.servicesImplementation;

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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pesco.example.authentication_service.clients.NotificationServiceClient;
import pesco.example.authentication_service.dtos.PageResponse;
import pesco.example.authentication_service.dtos.UserDTO;
import pesco.example.authentication_service.exceptions.Error;
import pesco.example.authentication_service.models.UserRecord;
import pesco.example.authentication_service.models.Users;
import pesco.example.authentication_service.repositories.UserRecordRepository;
import pesco.example.authentication_service.repositories.UsersRepository;
import pesco.example.authentication_service.services.PasswordResetTokenService;
import pesco.example.authentication_service.services.UserService;
import pesco.example.authentication_service.utils.KeyWrapper;

@Service
@Transactional
public class UserServiceImplementations implements UserService {

    private final NotificationServiceClient notificationServiceClient;
    private final UsersRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenService passwordResetTokenService;;
    private final KeyWrapper keysWrapper;
    private final UserRecordRepository userRecordRepository;

    public UserServiceImplementations(NotificationServiceClient notificationServiceClient, UsersRepository userRepository, PasswordEncoder passwordEncoder, PasswordResetTokenService passwordResetTokenService, KeyWrapper keysWrapper, UserRecordRepository userRecordRepository) {
        this.notificationServiceClient = notificationServiceClient;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordResetTokenService = passwordResetTokenService;
        this.keysWrapper = keysWrapper;
        this.userRecordRepository = userRecordRepository;
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
            record.setIsBlocked(true);
            userRecordRepository.save(record);
        }else{
            System.out.println("Nothing found");
        }

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

        if (profilePath != null && profilePath != "") {
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



}
