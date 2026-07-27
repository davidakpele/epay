package com.example.auth_user_service.services;

import com.example.auth_user_service.configurations.FileStorageConfig;
import com.example.auth_user_service.dtos.UserDTO;
import com.example.auth_user_service.interfaces.IUserRecordService;
import com.example.auth_user_service.models.UserAttempt;
import com.example.auth_user_service.models.UserRecord;
import com.example.auth_user_service.models.Users;
import com.example.auth_user_service.payloads.UpdateProfilePayload;
import com.example.auth_user_service.repositories.UserAttemptRepository;
import com.example.auth_user_service.repositories.UserRecordRepository;
import com.example.auth_user_service.repositories.UsersRepository;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.nio.file.DirectoryStream;
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

@Service
public class UserRecordService implements IUserRecordService{


    private final UsersRepository userRepository;
    private final UserRecordRepository userRecordRepository;
    private final FileStorageConfig fileStorageConfig;
    private final UserAttemptRepository userAttemptRepository;
    
    public UserRecordService(UsersRepository userRepository, UserRecordRepository userRecordRepository, FileStorageConfig fileStorageConfig, UserAttemptRepository userAttemptRepository) {
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
                updateUserRecord.setTransferPinSet(true);
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
            updateUserAccount.setBlocked(true);
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
                updateUserRecord.setProfileComplete(true);
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

            // Delete any existing image for this user, regardless of extension
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(uploadDir, id + ".*")) {
                for (Path existingFile : stream) {
                    Files.deleteIfExists(existingFile);
                }
            }

            String ext = StringUtils.getFilenameExtension(image.getOriginalFilename());
            String fileName = id + (ext != null ? "." + ext : "");
            Path filePath = uploadDir.resolve(fileName);

            Files.copy(image.getInputStream(), filePath);

            String profilePath = "/uploads/images/" + fileName;

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
        record.setBlocked(false); 
        userRepository.save(users);
        
        List<UserAttempt> attempts = userAttemptRepository.findByUserId(id);
        if (!attempts.isEmpty()) { 
            userAttemptRepository.deleteAll(attempts);
        }
        
        return ResponseEntity.ok(record.getFirstName() + " User account unlocked successfully.");
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

    // ── KYC Document Upload / Retrieve ──────────────────────────────────────

    private static final java.util.Set<String> ALLOWED_DOC_TYPES =
            java.util.Set.of("passport", "utility_bill");
    private static final java.util.Set<String> ALLOWED_DOC_MIME =
            java.util.Set.of(
                    "image/jpeg", "image/png", "image/webp",
                    "application/pdf",
                    // Accept raw binary uploads (sent when browser can't determine MIME)
                    "application/octet-stream"
            );

    @Override
    public ResponseEntity<?> uploadKycDocument(Long userId, String docType, MultipartFile file) {
        if (!ALLOWED_DOC_TYPES.contains(docType)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "message",
                            "Invalid document type. Use 'passport' or 'utility_bill'."));
        }

        Optional<UserRecord> recordOpt = userRecordRepository.findByUserId(userId);
        if (recordOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", "error", "message", "User record not found."));
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_DOC_MIME.contains(contentType)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "message",
                            "Unsupported file type. Upload JPEG, PNG, WebP, or PDF."));
        }

        try {
            Path uploadDir = Paths.get(fileStorageConfig.getUploadDir(), "kyc", String.valueOf(userId))
                    .toAbsolutePath().normalize();
            Files.createDirectories(uploadDir);

            // Delete any existing file for this doc type
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(uploadDir, docType + ".*")) {
                for (Path existing : stream) Files.deleteIfExists(existing);
            }

            // Read bytes once (needed for magic-byte detection)
            byte[] fileBytes = file.getBytes();

            // Derive extension: prefer original filename, fall back to magic bytes
            String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
            if (ext == null || ext.isBlank()) {
                // Detect by magic bytes
                if (fileBytes.length >= 4 &&
                    fileBytes[0] == 0x25 && fileBytes[1] == 0x50 &&
                    fileBytes[2] == 0x44 && fileBytes[3] == 0x46) {
                    ext = "pdf"; // %PDF
                } else if (fileBytes.length >= 3 &&
                    (fileBytes[0] & 0xFF) == 0xFF && (fileBytes[1] & 0xFF) == 0xD8) {
                    ext = "jpg"; // JPEG
                } else if (fileBytes.length >= 8 &&
                    (fileBytes[1] == 'P') && (fileBytes[2] == 'N') && (fileBytes[3] == 'G')) {
                    ext = "png"; // PNG
                } else {
                    ext = "bin";
                }
            }

            String fileName = docType + "." + ext;
            Path filePath = uploadDir.resolve(fileName);
            Files.write(filePath, fileBytes);

            String storedPath = "/uploads/images/kyc/" + userId + "/" + fileName;

            UserRecord record = recordOpt.get();
            if ("passport".equals(docType)) {
                record.setPassportDoc(storedPath);
            } else {
                record.setUtilityBillDoc(storedPath);
            }
            userRecordRepository.save(record);

            return ResponseEntity.ok(Map.of(
                    "status",   "success",
                    "message",  "KYC document uploaded successfully.",
                    "docType",  docType,
                    "fileUrl",  storedPath
            ));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", "Error saving document: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> getKycDocument(Long userId, String docType) {
        if (!ALLOWED_DOC_TYPES.contains(docType)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "message",
                            "Invalid document type. Use 'passport' or 'utility_bill'."));
        }

        Optional<UserRecord> recordOpt = userRecordRepository.findByUserId(userId);
        if (recordOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", "error", "message", "User record not found."));
        }

        UserRecord record = recordOpt.get();
        String path = "passport".equals(docType) ? record.getPassportDoc() : record.getUtilityBillDoc();

        if (path == null || path.isBlank()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", "error", "message", "No document found for type: " + docType));
        }

        // Serve the file as a byte stream
        try {
            Path uploadDir = Paths.get(fileStorageConfig.getUploadDir()).toAbsolutePath().normalize();
            // path stored as /uploads/images/kyc/... — strip the /uploads/images/ prefix
            String relativePath = path.replaceFirst("^/uploads/images/", "");
            Path filePath = uploadDir.resolve(relativePath).normalize();

            if (!filePath.startsWith(uploadDir)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("status", "error", "message", "Access denied."));
            }

            if (!Files.exists(filePath)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("status", "error", "message", "Document file not found on server."));
            }

            byte[] data  = Files.readAllBytes(filePath);
            String mime  = Files.probeContentType(filePath);
            if (mime == null) {
                // Fallback by extension
                String fname = filePath.getFileName().toString().toLowerCase();
                if (fname.endsWith(".pdf"))  mime = "application/pdf";
                else if (fname.endsWith(".png"))  mime = "image/png";
                else if (fname.endsWith(".webp")) mime = "image/webp";
                else mime = "image/jpeg";
            }

            return ResponseEntity.ok()
                    .header("Content-Type", mime)
                    .header("Content-Disposition", "inline; filename=\"" + filePath.getFileName() + "\"")
                    .body(data);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", "Error reading document."));
        }
    }
}
