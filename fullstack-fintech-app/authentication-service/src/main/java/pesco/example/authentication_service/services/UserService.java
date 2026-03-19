package pesco.example.authentication_service.services;

import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import pesco.example.authentication_service.dtos.PageResponse;
import pesco.example.authentication_service.dtos.UserDTO;
import pesco.example.authentication_service.models.Users;
import pesco.example.authentication_service.responses.UserStatisticsResponse;

public interface UserService {

    Users getUserByUsername(String username);

    Users getUserById(Long userId);

    Optional<Users> findById(Long id);

    ResponseEntity<?> resetPassword(Long userId, String password, Authentication authentication);

    Object deactivateAccount(Long id, Authentication authentication);

    ResponseEntity<?> forgetPassword(String email);

    ResponseEntity<?> updateUserProfile(String username, String email, String gender, String profilePath);

    PageResponse<UserDTO> getAllUsersPaginated(int page, int size);

    public Long countAllUsers();

    UserDTO findUserWithRecordById(Long id);

    void deleteUserAccount(String id);

    void lockUserAccount(Long id, boolean lock);

    void blockUserAccount(Long id, boolean block);

    UserDTO getUserDetails(Long id);

    UserStatisticsResponse getUserStatistics(String period);
}
