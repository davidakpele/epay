package com.epay.auth.interfaces;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import com.epay.domain.auth.entity.TwoFactorAuthentication;
import com.epay.domain.auth.entity.User;
import com.epay.domain.auth.input.OTPRequest;

public interface ITwoFactorAuthenticationService {

    TwoFactorAuthentication createTwoFactorOtp(User authUser, String otp, String jwtToken);

    TwoFactorAuthentication findByUser(Long userid);

    TwoFactorAuthentication findById(Long id);

    boolean verifyTwoFactorOtp(TwoFactorAuthentication twoFactorOTP, String otp);

    void deleteTwoFactorOtp(TwoFactorAuthentication twoFactorOTP);

    ResponseEntity<?> findByToken(String token);

    ResponseEntity<?> verifyUserTwoFactorOtp(OTPRequest reqOtpPayload);

    ResponseEntity<?> enableTwoFactorKey(Boolean enable2fa, Authentication authentication);
}