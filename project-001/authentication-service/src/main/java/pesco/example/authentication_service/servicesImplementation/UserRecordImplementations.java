package pesco.example.authentication_service.servicesImplementation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import pesco.example.authentication_service.configurations.FileStorageConfig;
import pesco.example.authentication_service.dtos.UserDTO;
import pesco.example.authentication_service.models.UserAttempt;
import pesco.example.authentication_service.models.UserRecord;
import pesco.example.authentication_service.models.Users;
import pesco.example.authentication_service.payloads.UpdateProfilePayload;
import pesco.example.authentication_service.repositories.UserAttemptRepository;
import pesco.example.authentication_service.repositories.UserRecordRepository;
import pesco.example.authentication_service.repositories.UsersRepository;
import pesco.example.authentication_service.services.UserRecordService;

@Service
public class UserRecordImplementations implements UserRecordService {

    private final UsersRepository userRepository;
    private final UserRecordRepository userRecordRepository;
    private final FileStorageConfig fileStorageConfig;
    private final UserAttemptRepository userAttemptRepository;
    
    public UserRecordImplementations(UsersRepository userRepository, UserRecordRepository userRecordRepository, FileStorageConfig fileStorageConfig, UserAttemptRepository userAttemptRepository) {
        this.userRepository = userRepository;
        this.userRecordRepository = userRecordRepository;
        this.fileStorageConfig = fileStorageConfig;
        this.userAttemptRepository = userAttemptRepository;
    }
    
    @Override
    public UserDTO getUserDetailsById(Long id) {
        Users user = userRepository.findUserWithRecordById(id);
        return UserDTO.fromEntity(user);
    }

    @Override
    public UserDTO getUserByUsername(String username) {
        Optional<Users> getUser = userRepository.findByUsername(username);

        if (getUser.isEmpty()) {
            return null;
        }

        Users user = userRepository.findUserWithRecordById(getUser.get().getId());
        if (user == null) {
            return null;
        }

        return UserDTO.fromEntity(user);
    }    

    @Override
    public Optional<UserRecord> getUserReferralCode(Long id) {
        var userRecord = userRecordRepository.findByUserId(id);
        return userRecord;
    }

    @Override
    public boolean isLockedAccount(Long userId) {
        return userRecordRepository.isUserAccountLocked(userId);
    }

    @Override
    public boolean isBlockedAccount(Long userId) {
        return userRecordRepository.isUserAccountBlocked(userId);
    }

    @Override
    public Optional<UserRecord> getUserNames(Long userId) {
        return userRecordRepository.findByUserId(userId);
    }

    @Override
    public ResponseEntity<?> updateUserRecordTransferPinStatus(Long id, Authentication authentication) {
        Optional<Users> GetUser = userRepository.findById(id);
        String NewUsername = authentication.getName();
        if (GetUser.isPresent() && NewUsername.equals(GetUser.get().getUsername())) {
            Optional<UserRecord> userRecord = userRecordRepository.findByUserId(id);
            if (userRecord.isPresent()) {
                UserRecord updateUserRecord = userRecord.get();
                updateUserRecord.setIsTransferPinSet(true);
                userRecordRepository.save(updateUserRecord);
            }
            return ResponseEntity.ok("Transfer Pin set successfully.");
        }

        return null;
    }

    @Override
    public UserDTO findPublicUserByUsername(String username) {
        Optional<Users> GetUser = userRepository.findByUsername(username);
        if (GetUser.isPresent()) {
            Users user = userRepository.findUserWithRecordById(GetUser.get().getId());
            return UserDTO.fromEntity(user);
        }

        return null;
    }

    @Override
    public UserDTO findPublicUserByUserId(Long userId) {
        Optional<Users> GetUser = userRepository.findById(userId);
        if (GetUser.isPresent()) {
            Users user = userRepository.findUserWithRecordById(GetUser.get().getId());
            return UserDTO.fromEntity(user);
        }

        return null;
    }

    @Override
    public ResponseEntity<?> lockUserAccount(Long userId) {
        Optional<UserRecord> user = userRecordRepository.findByUserId(userId);
        if (user != null && user.isPresent()) {
            UserRecord updateUserAccount = user.get();

            updateUserAccount.setLocked(true);
            userRecordRepository.save(updateUserAccount);
            return ResponseEntity.ok("User account successfully lock.");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
    }

    @Override
    public ResponseEntity<?> blockUserAccount(Long userId) {
        Optional<UserRecord> user = userRecordRepository.findByUserId(userId);
        if (user != null && user.isPresent()) {
            UserRecord updateUserAccount = user.get();
            updateUserAccount.setIsBlocked(true);
            userRecordRepository.save(updateUserAccount);
            return ResponseEntity.ok("User account successfully block.");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
    }

    @Override
    public ResponseEntity<?> updateProfile(Long id, UpdateProfilePayload updateProfilePayload,Authentication authentication) {
        Optional<Users> getUser = userRepository.findById(id);
        if (getUser.isPresent()) { 
            
            Users user = getUser.get();
            user.setEmail(updateProfilePayload.getEmail());
            userRepository.save(user);

            Optional<UserRecord> userRecord = userRecordRepository.findByUserId(id);
            UserRecord updatedRecord = null;
            
            if (userRecord.isPresent()) {
                UserRecord updateUserRecord = userRecord.get();
                updateUserRecord.setFirstName(updateProfilePayload.getFirstName());
                updateUserRecord.setLastName(updateProfilePayload.getLastName());
                updateUserRecord.setGender(updateProfilePayload.getGender());
                updateUserRecord.setDateofBirth(updateProfilePayload.getDob());
                updateUserRecord.setAddress(updateProfilePayload.getAddress());
                updateUserRecord.setTelephone(updateProfilePayload.getTelephone());

                if(updateProfilePayload.getCountry() != null && !updateProfilePayload.getCountry().isEmpty()) {
                    updateUserRecord.setCountry(updateProfilePayload.getCountry());
                }
                if(updateProfilePayload.getState() != null && !updateProfilePayload.getState().isEmpty()) {
                    updateUserRecord.setState(updateProfilePayload.getState());
                }
                if(updateProfilePayload.getCity() != null && !updateProfilePayload.getCity().isEmpty()) {
                    updateUserRecord.setCity(updateProfilePayload.getCity());
                }
                updateUserRecord.setIsProfileComplete(true);
                updateUserRecord.setUser(getUser.get());
                updatedRecord = userRecordRepository.save(updateUserRecord);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Profile updated successfully.");
            response.put("userId", id);
            response.put("is_profile_complete", updatedRecord != null && updatedRecord.getIsProfileComplete());
            if (updatedRecord != null) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("firstName", updatedRecord.getFirstName());
                userData.put("lastName", updatedRecord.getLastName());
                userData.put("email", getUser.get().getEmail()); 
                userData.put("gender", updatedRecord.getGender());
                userData.put("telephone", updatedRecord.getTelephone());
                userData.put("address", updatedRecord.getAddress());
                userData.put("dob", updatedRecord.getDateofBirth());
                userData.put("country", updatedRecord.getCountry());
                userData.put("state", updatedRecord.getState());
                userData.put("city", updatedRecord.getCity());
                
                response.put("user", userData);
            }

            return ResponseEntity.ok(response);
        }
        
        Map<String, Object> error = new HashMap<>();
        error.put("status", "error");
        error.put("message", "Unauthorized or user not found.");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @Override
    public ResponseEntity<?> uploadProfileImage(Long id, MultipartFile image) {

        Optional<Users> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", "error", "message", "Unauthorized or user not found."));
        }

        Optional<UserRecord> recordOpt = userRecordRepository.findByUserId(id);
        if (recordOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", "error", "message", "User record not found."));
        }

        try {
            Path uploadDir = Paths.get(fileStorageConfig.getUploadDir()).toAbsolutePath().normalize();
            Files.createDirectories(uploadDir);

            String ext = StringUtils.getFilenameExtension(image.getOriginalFilename());
            String fileName = id + (ext != null ? "." + ext : "");
            Path filePath = uploadDir.resolve(fileName);

            Files.deleteIfExists(filePath);
            Files.copy(image.getInputStream(), filePath);

            String profilePath = "/image/" + fileName;

            UserRecord userRecord = recordOpt.get();
            userRecord.setPhoto(profilePath);  
            userRecord.setUser(userOpt.get());
            userRecordRepository.save(userRecord);

            return ResponseEntity.ok(
                    Map.of(
                            "status", "success",
                            "message", "Profile image uploaded successfully.",
                            "userId", id,
                            "imageUrl", profilePath  
                    )
            );

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", "Error uploading image."));
        }
    }

    @Override
    public Optional<UserRecord> findByTelephone(String telephone) {
        return userRecordRepository.findByTelephone(telephone);
    }

    @Override
    public ResponseEntity<?> unlockedAccount(Long id) {
        Users users = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));
        UserRecord record = users.getRecords().get(0);
        record.setLocked(false);  
        record.setIsBlocked(false); 
        userRepository.save(users);
        
        UserAttempt attempt = userAttemptRepository.findByUserId(id);
        if (attempt != null) {
            userAttemptRepository.deleteById(attempt.getId());
        }
        
        return ResponseEntity.ok(record.getFirstName()+ " User account unlocked successfully.");
    }

    @Override
    public ResponseEntity<?> removeProfileImage(Long id) {
        Optional<UserRecord> recordOpt = userRecordRepository.findByUserId(id);
        if (recordOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", "error", "message", "User record not found."));
        }

        UserRecord userRecord = recordOpt.get();
        String profilePath = userRecord.getPhoto();

        if (profilePath != null && !profilePath.isEmpty()) {
            try {
                Path uploadDir = Paths.get(fileStorageConfig.getUploadDir()).toAbsolutePath().normalize();
                Path filePath = uploadDir.resolve(profilePath.replaceFirst("^/", "")); 
                Files.deleteIfExists(filePath);
                userRecord.setPhoto(null);
                userRecordRepository.save(userRecord);

                return ResponseEntity.ok(Map.of("status", "success", "message", "Profile image removed."));
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("status", "error", "message", "Error removing image."));
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("status", "error", "message", "No profile image to remove."));
        }
    }





}
