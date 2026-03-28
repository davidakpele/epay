package com.example.auth_user_service.interfaces;

import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import com.example.auth_user_service.dtos.UserDTO;
import com.example.auth_user_service.models.UserRecord;
import com.example.auth_user_service.payloads.UpdateProfilePayload;

public interface IUserRecordService {

    UserDTO getUserDetailsById(Long id);

    UserDTO getUserByUsername(String username);

    UserDTO findPublicUserByUsername(String username);

    Optional<UserRecord> getUserReferralCode(Long id);

    Optional<UserRecord> getUserNames(Long userId);

    boolean isLockedAccount(Long userId);

    boolean isBlockedAccount(Long userId);

    ResponseEntity<?> updateUserRecordTransferPinStatus(Long id, Authentication authentication);

    UserDTO findPublicUserByUserId(Long userId);

    ResponseEntity<?> lockUserAccount(Long userId);

    ResponseEntity<?> blockUserAccount(Long userId);

    ResponseEntity<?> updateProfile(Long id, UpdateProfilePayload updateProfilePayload, Authentication authentication);

    ResponseEntity<?> uploadProfileImage(Long id, MultipartFile image);

    Optional<UserRecord> findByTelephone(String telephone);

    ResponseEntity<?> unlockedAccount(Long id);

    ResponseEntity<?> removeProfileImage(Long id);

}
