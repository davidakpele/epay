package com.example.auth_user_service.interfaces;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import com.example.auth_user_service.models.TwoFactorAuthentication;
import com.example.auth_user_service.models.Users;
import com.example.auth_user_service.payloads.OTPRequest;

public interface ITwoFactorAuthenticationService {

    TwoFactorAuthentication createTwoFactorOtp(Users authUser, String otp, String jwtToken);

    TwoFactorAuthentication findByUser(Long userid);

    TwoFactorAuthentication findById(Long id);

    boolean verifyTwoFactorOtp(TwoFactorAuthentication twoFactorOTP, String otp);

    void deleteTwoFactorOtp(TwoFactorAuthentication twoFactorOTP);

    ResponseEntity<?> findByToken(String token);

    ResponseEntity<?> verifyUserTwoFactorOtp(OTPRequest reqOtpPayload);

    ResponseEntity<?> enableTwoFactorKey(Boolean enable2fa, Authentication authentication);
}