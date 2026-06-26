package com.example.auth_user_service.controllers;


import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.example.auth_user_service.dtos.UserDTO;
import com.example.auth_user_service.interfaces.ITwoFactorAuthenticationService;
import com.example.auth_user_service.interfaces.IUserAccountCasesReportService;
import com.example.auth_user_service.interfaces.IUserRecordService;
import com.example.auth_user_service.interfaces.IUserService;
import com.example.auth_user_service.payloads.DeleteteAccountRequest;
import com.example.auth_user_service.exceptions.Error;


@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/settings")
public class SettingController {

    private final IUserService userServices;
    private final IUserRecordService userRecordService;
    private final ITwoFactorAuthenticationService twoFactorAuthenticationService;
    private final IUserAccountCasesReportService userAccountCasesReportService;

    public SettingController(IUserService userServices, IUserRecordService userRecordService, ITwoFactorAuthenticationService twoFactorAuthenticationService, IUserAccountCasesReportService userAccountCasesReportService) {
        this.userServices = userServices;
        this.userRecordService = userRecordService;
        this.twoFactorAuthenticationService = twoFactorAuthenticationService;
        this.userAccountCasesReportService = userAccountCasesReportService;
    }
    

    @PreAuthorize("hasRole('USER')")
    @PostMapping(value = "/upload-profile-image/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadProfileImage(@PathVariable Long id, @RequestParam("image") MultipartFile image) {
        if (id == null || id <= 0) {
            return Error.createResponse("Invalid request sent.", HttpStatus.BAD_REQUEST, "User Id is missing");
        }

        if (image == null || image.isEmpty()) {
            return Error.createResponse("No image file provided.", HttpStatus.BAD_REQUEST, "Image is required");
        }

        if (userServices.getUserById(id) == null) {
            return Error.createResponse("User with ID " + id + " does not exist.", HttpStatus.BAD_REQUEST, "User does not exist");
        }
        ResponseEntity<?> response = userRecordService.uploadProfileImage(id, image);
        return response;
    }


    @GetMapping("/user/{id}") 
    public ResponseEntity<?> findById(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return Error.createResponse(
                "Invalid request sent.",
                HttpStatus.BAD_REQUEST,
                "User Id is missing"
            );
        }
        UserDTO userDTO = userServices.findUserWithRecordById(id);

        if (userDTO == null) {
            return Error.createResponse(
                "User with ID " + id + " does not exist.",
                HttpStatus.NOT_FOUND,
                "User does not exist"
            );
        }
        return ResponseEntity.ok(userDTO);
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/enable-twofactor")
    public ResponseEntity<?> verifyUserOtp(@RequestBody Map<String, Boolean> requestPayload, Authentication authentication) {
        Boolean enable2FA = requestPayload.get("enable2FA");

        if (enable2FA == null) {
            return ResponseEntity.badRequest().body("Invalid request payload");
        }
        try {
            return ResponseEntity.ok(twoFactorAuthenticationService.enableTwoFactorKey(enable2FA, authentication));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating Two-Factor Authentication");
        }
    }

    @DeleteMapping("/remove-profile-image/{id}")
    public ResponseEntity<?> removeProfileImage(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return Error.createResponse("Invalid request sent.", HttpStatus.BAD_REQUEST, "User Id is missing");
        }

        if (userServices.getUserById(id) == null) {
            return Error.createResponse("User with ID " + id + " does not exist.", HttpStatus.BAD_REQUEST, "User does not exist");
        }
        ResponseEntity<?> response = userRecordService.removeProfileImage(id);
        return response;
    }

    // Delete user account
    @DeleteMapping("/delete-account/{id}")
    public ResponseEntity<?> deleteAccount(@PathVariable Long id, @RequestBody DeleteteAccountRequest deleteAccountRequest) {
        if (id == null || id <= 0) {
            return Error.createResponse("Invalid request sent.", HttpStatus.BAD_REQUEST, "User Id is missing");
        }
        if (userServices.getUserById(id) == null) {
            return Error.createResponse("User with ID " + id + " does not exist.", HttpStatus.BAD_REQUEST, "User does not exist");
        }
        ResponseEntity<?> response = userAccountCasesReportService.deleteUserAccount(id, deleteAccountRequest);
        return response;
    }

    
}
