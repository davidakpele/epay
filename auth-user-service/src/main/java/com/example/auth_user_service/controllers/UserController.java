package com.example.auth_user_service.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.example.auth_user_service.components.KeyWrapper;
import com.example.auth_user_service.configurations.FileStorageConfig;
import com.example.auth_user_service.dtos.PageResponse;
import com.example.auth_user_service.dtos.UserDTO;
import com.example.auth_user_service.interfaces.IPasswordResetTokenService;
import com.example.auth_user_service.interfaces.IUserAttemptService;
import com.example.auth_user_service.interfaces.IUserRecordService;
import com.example.auth_user_service.interfaces.IUserService;
import com.example.auth_user_service.models.Users;
import com.example.auth_user_service.payloads.ChangePasswordRequest;
import com.example.auth_user_service.payloads.UpdateProfilePayload;
import com.example.auth_user_service.payloads.UserSignUpRequest;
import com.example.auth_user_service.responses.UserStatisticsResponse;
import jakarta.servlet.http.HttpServletResponse;
import com.example.auth_user_service.exceptions.Error;

@RestController
@RequestMapping("/user")
public class UserController {

    private final IUserService userServices;
    private final IUserRecordService userRecordService;
    private final FileStorageConfig fileStorageConfig;
    private final KeyWrapper keysWrapper;
    private final IPasswordResetTokenService passwordResetTokenService;
    private final IUserAttemptService userAttemptService;


    public UserController(IUserService userServices, IUserRecordService userRecordService, FileStorageConfig fileStorageConfig, KeyWrapper keysWrapper, IPasswordResetTokenService passwordResetTokenService, IUserAttemptService userAttemptService) {
        this.userServices = userServices;
        this.userRecordService = userRecordService;
        this.fileStorageConfig = fileStorageConfig;
        this.keysWrapper = keysWrapper;
        this.passwordResetTokenService = passwordResetTokenService;
        this.userAttemptService = userAttemptService;
    }
    
    
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/my-referral/{id}")
    public ResponseEntity<?> referral(@PathVariable Long id, Authentication authentication) {
        if (id == null || id <= 0) {
            return Error.createResponse("Invalid request sent.", HttpStatus.BAD_REQUEST, "User Id is missing");
        }

        else if (userServices.getUserById(id) == null) {
            return Error.createResponse("User with ID " + id + " does not exist.", HttpStatus.BAD_REQUEST,
                    "User does not exist");
        }
        Optional<Users> user = userServices.findById(id);
        String username = authentication.getName();
        if (!user.get().getUsername().equals(username)) {
            return Error.createResponse("Unauthorized access", HttpStatus.FORBIDDEN, "Access Danied");
        }
        return ResponseEntity.ok(userRecordService.getUserReferralCode(id));
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("/settings/reset-password/{id}")
    public ResponseEntity<?> resetPassword(@PathVariable Long id, @RequestBody UserSignUpRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(userServices.resetPassword(id, request.getPassword(), authentication));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PutMapping("/settings/deactivate-account/{id}")
    public ResponseEntity<?> deactivateAccount(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(userServices.deactivateAccount(id, authentication));
    }

    @GetMapping("/referral-code/{id}")
    public ResponseEntity<?> referralCode(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(userRecordService.getUserReferralCode(id));
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(Authentication authentication) {
        String username = authentication.getName();
        Users user = userServices.getUserByUsername(username);
        if (!username.equals(user.getUsername())) {
            return Error.createResponse(
                    "UNAUTHORIZE ACCESS", HttpStatus.FORBIDDEN,
                    "You dont have access to the endpoints");
        }
        return ResponseEntity.ok(user);
    }
    
    @PostMapping("/settings/update/user")
    public ResponseEntity<?> updateUser(
            @RequestParam(value = "profile", required = false) MultipartFile profile,
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("gender") String gender, Authentication authentication) {
        String requestUsername = authentication.getName();
        Users user = userServices.getUserByUsername(requestUsername);
        if (!username.equals(user.getUsername())) {
            return Error.createResponse(
                    "UNAUTHORIZE ACCESS", HttpStatus.FORBIDDEN,
                    "You dont have access to the endpoints");
        }
        try {
            String profilePath = null;
            if (profile != null && !profile.isEmpty()) {
                Path uploadDir = Paths.get(fileStorageConfig.getUploadDir());
                if (Files.notExists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                }
                String userId = user.getId().toString();
                String extension = StringUtils.getFilenameExtension(profile.getOriginalFilename());
                String newFileName = userId + (extension != null ? "." + extension : "");
                Path filePath = uploadDir.resolve(newFileName);
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                }
                Files.copy(profile.getInputStream(), filePath);
                String baseUrl = keysWrapper.getAssetUrl();
                profilePath = baseUrl + "/image/" + newFileName;
            }

            return ResponseEntity.ok(userServices.updateUserProfile(username, email, gender, profilePath));
        } catch (IOException e) {
            return new ResponseEntity<>("Failed to upload file: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("/settings/updatepassword")
    public ResponseEntity<?> updatePassword(@RequestBody ChangePasswordRequest request, Authentication authentication) {
        if (request.getPassword().isEmpty()) {
            return Error.createResponse("Password is require*", HttpStatus.BAD_REQUEST,
                    "Password can not be empty");
        }
        if (request.getConfirmPassword().isEmpty()) {
            return Error.createResponse("Confirm Password is require*", HttpStatus.BAD_REQUEST,
                    "Confirm Password can not be empty");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return Error.createResponse("Passwords do not match*", HttpStatus.BAD_REQUEST,
                    "Password and Confirm Password must be the same");
        }

        return ResponseEntity.ok(passwordResetTokenService.resetPassword(request, authentication));
    } 

    @PreAuthorize("hasRole('USER')")
    @PutMapping("/{id}/updateUserRecord/transferPin/status")
    public ResponseEntity<?> updateUserRecordTransferPinStatus(@PathVariable Long id, Authentication authentication) {
        if (id == null) {
            return Error.createResponse("User Id is requir.*", HttpStatus.BAD_REQUEST,
                    "Please provide you Id.");
        }
        return userRecordService.updateUserRecordTransferPinStatus(id, authentication);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<?> findUserByUsername(@PathVariable String username) {
        if (username == null || username.isEmpty()) {
            return Error.createResponse("Username is require.*", HttpStatus.BAD_REQUEST, "Username is require.*");
        } else if (userServices.getUserByUsername(username) == null) {
            return Error.createResponse("User with username " + username + " does not exist.", HttpStatus.BAD_REQUEST,
                    "User does not exist");
        }
        UserDTO userDTO = userRecordService.findPublicUserByUsername(username);

        return ResponseEntity.ok(userDTO);
    }

    @PutMapping("/{id}/account/lock")
    public ResponseEntity<?> lockUserAccount(@PathVariable Long userId, HttpServletResponse response) {
        if (userId == null) {
            return Error.createResponse("User Id is require.*", HttpStatus.BAD_REQUEST, "Username is require.*");
        } else if (userServices.getUserById(userId) == null) {
            return Error.createResponse("User with user Id " + userId + " does not exist.", HttpStatus.BAD_REQUEST,
                    "User does not exist");
        }
        return userRecordService.lockUserAccount(userId);
    }

    @PutMapping("/{id}/account/block")
    public ResponseEntity<?> blockUserAccount(@PathVariable Long userId, HttpServletResponse response) {
        if (userId == null) {
            return Error.createResponse("User Id is require.*", HttpStatus.BAD_REQUEST, "Username is require.*");
        } else if (userServices.getUserById(userId) == null) {
            return Error.createResponse("User with user Id " + userId + " does not exist.", HttpStatus.BAD_REQUEST,
                    "User does not exist");
        }
        return userRecordService.blockUserAccount(userId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id, HttpServletResponse response) {
        if (id == null || id <= 0) {
            return Error.createResponse("Invalid request sent.", HttpStatus.BAD_REQUEST, "User Id is missing");
        } else if (userServices.getUserById(id) == null) {
            return Error.createResponse("User with ID " + id + " does not exist.", HttpStatus.BAD_REQUEST,
                    "User does not exist");
        }
        UserDTO userDTO = userRecordService.getUserDetailsById(id);

        return ResponseEntity.ok(userDTO);
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("/profile/{id}")
    public ResponseEntity<?> updateProfile(@PathVariable Long id, @RequestBody UpdateProfilePayload updateProfilePayload, HttpServletResponse response,
            Authentication authentication) {
        if (id == null || id <= 0) {
            return Error.createResponse("Invalid request sent.", HttpStatus.BAD_REQUEST, "User Id is missing");
        } else if (userServices.getUserById(id) == null) {
            return Error.createResponse("User with ID " + id + " does not exist.", HttpStatus.BAD_REQUEST,
                    "User does not exist");
        }
        return userRecordService.updateProfile(id, updateProfilePayload, authentication);
    }

    @GetMapping("/list")
    public ResponseEntity<PageResponse<UserDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {

        PageResponse<UserDTO> response = userServices.getAllUsersPaginated(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countAllUsers() {
        Long totalUsers = userServices.countAllUsers();
        return ResponseEntity.ok(totalUsers);
    }

    @PostMapping("/{id}/block")
    public ResponseEntity<?> blockUserAccount(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        boolean block = Boolean.TRUE.equals(body.get("block"));
        userServices.blockUserAccount(id, block);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", block ? "User blocked successfully" : "User unblocked successfully"
        ));
    }

    @PostMapping("/{id}/lock")
    public ResponseEntity<?> lockUserAccount(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        boolean lock = Boolean.TRUE.equals(body.get("lock"));
        userServices.lockUserAccount(id, lock);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", lock ? "User locked successfully" : "User unlocked successfully"
        ));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUserAccount(@PathVariable String id) {
        userServices.deleteUserAccount(id);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "User account successfully deleted!"));
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{id}/send-reset-link")
    public ResponseEntity<?> sendResetPasswordLink(
            @PathVariable Long id,
            Authentication authentication) {
        if (id == null || id <= 0) {
            return Error.createResponse("Invalid user ID.", HttpStatus.BAD_REQUEST, "User ID is missing.");
        }
        return userServices.sendPasswordResetLinkToSelf(id, authentication);
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{id}/suspend")
    public ResponseEntity<?> suspendAccount(
            @PathVariable Long id,
            Authentication authentication) {
        if (id == null || id <= 0) {
            return Error.createResponse("Invalid user ID.", HttpStatus.BAD_REQUEST, "User ID is missing.");
        }
        return userServices.suspendAccount(id, authentication);
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{id}/kyc/upload")
    public ResponseEntity<?> uploadKycDocument(
            @PathVariable Long id,
            @RequestParam("docType") String docType,
            @RequestParam(value = "file", required = false) MultipartFile multipartFile,
            jakarta.servlet.http.HttpServletRequest httpRequest) {

        if (id == null || id <= 0) {
            return Error.createResponse("Invalid user ID.", HttpStatus.BAD_REQUEST, "User ID is missing.");
        }

        String contentType = httpRequest.getContentType();
        boolean isMultipart = contentType != null && contentType.toLowerCase().startsWith("multipart/");

        MultipartFile file = multipartFile;

        if (file == null || file.isEmpty()) {
            if (isMultipart) {
                return Error.createResponse(
                        "No file part found.",
                        HttpStatus.BAD_REQUEST,
                        "Expected a multipart field named 'file'. Check the field name in your form-data.");
            }

            try {
                String ct = contentType != null ? contentType : "application/octet-stream";
                byte[] bytes = httpRequest.getInputStream().readAllBytes();
                if (bytes.length == 0) {
                    return Error.createResponse("No file provided.", HttpStatus.BAD_REQUEST, "Please upload a file.");
                }
                final byte[] fb = bytes;
                final String fct = ct;
                String ext = ct.contains("pdf") ? "pdf" : ct.contains("png") ? "png"
                        : ct.contains("webp") ? "webp" : "jpg";
                final String fn = docType + "." + ext;
                file = new MultipartFile() {
                    public String getName()                    { return "file"; }
                    public String getOriginalFilename()        { return fn; }
                    public String getContentType()             { return fct; }
                    public boolean isEmpty()                   { return fb.length == 0; }
                    public long   getSize()                    { return fb.length; }
                    public byte[] getBytes()                   { return fb; }
                    public java.io.InputStream getInputStream(){ return new java.io.ByteArrayInputStream(fb); }
                    public void transferTo(java.io.File d) throws java.io.IOException { try(var o=new java.io.FileOutputStream(d)){o.write(fb);} }
                    public org.springframework.core.io.Resource getResource() {
                        return new org.springframework.core.io.ByteArrayResource(fb) {
                            @Override public String getFilename() { return fn; }
                        };
                    }
                    public void transferTo(java.nio.file.Path p) throws java.io.IOException { java.nio.file.Files.write(p, fb); }
                };
            } catch (java.io.IOException e) {
                return Error.createResponse("Failed to read file.", HttpStatus.BAD_REQUEST, e.getMessage());
            }
        }

        return userRecordService.uploadKycDocument(id, docType, file);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{id}/kyc/document")
    public ResponseEntity<?> getKycDocument(
            @PathVariable Long id,
            @RequestParam("docType") String docType) {

        if (id == null || id <= 0) {
            return Error.createResponse("Invalid user ID.", HttpStatus.BAD_REQUEST, "User ID is missing.");
        }
        return userRecordService.getKycDocument(id, docType);
    }

    @GetMapping("/{id}/edit")
    public ResponseEntity<UserDTO> getUserEditDetails(@PathVariable Long id) {
        return ResponseEntity.ok(userServices.getUserDetails(id));
    }

    @GetMapping("/{id}/view")
    public ResponseEntity<UserDTO> getUserViewDetails(@PathVariable Long id) {
        return ResponseEntity.ok(userServices.getUserDetails(id));
    }

    @GetMapping("/statistics")
    public ResponseEntity<UserStatisticsResponse> getUserStatistics(@RequestParam(defaultValue = "MONTHLY") String period) {
        return ResponseEntity.ok(userServices.getUserStatistics(period));
    }

    @DeleteMapping("/attempt/delete/{userId}")
    public ResponseEntity<?> deleteAttempt(@PathVariable Long userId) {
        return userAttemptService.deleteAttempt(userId);
    }

}
