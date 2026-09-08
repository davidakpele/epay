package com.epay.auth.service;

import com.epay.auth.interfaces.ITwoFactorAuthenticationService;
import com.epay.auth.interfaces.IUserTracerService;
import com.epay.common.config.interfaces.IJwtService;
import com.epay.common.exception.BadRequestException;
import com.epay.common.exception.ErrorCode;
import com.epay.common.exception.ResourceNotFoundException;
import com.epay.common.interfaces.IAuthNotificationPublisher;
import com.epay.domain.auth.entity.TwoFactorAuthentication;
import com.epay.domain.auth.entity.User;
import com.epay.domain.auth.entity.UserRecord;
import com.epay.domain.auth.entity.UserTracer;
import com.epay.domain.auth.input.OTPRequest;
import com.epay.domain.auth.repository.TwoFactorOTPRepository;
import com.epay.domain.auth.repository.UserRecordRepository;
import com.epay.domain.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwoFactorAuthenticationService implements ITwoFactorAuthenticationService {

    private static final int    EXPIRATION_MINUTES = 1;
    private static final DateTimeFormatter EVT_FMT =DateTimeFormatter.ofPattern("EEE, dd MMM yyyy hh:mm:ss a");

    private final TwoFactorOTPRepository   twoFactorOTPRepository;
    private final UserRepository           userRepository;
    private final IJwtService              jwtService;
    private final UserRecordRepository     userRecordRepository;
    private final IAuthNotificationPublisher notificationPublisher;
    private final IUserTracerService       userTracerService;
    
    @Override
    public TwoFactorAuthentication createTwoFactorOtp(User authUser, String otp, String jwtToken) {
        TwoFactorAuthentication tfa = new TwoFactorAuthentication();
        tfa.setOtp(otp);
        tfa.setToken(jwtToken);
        tfa.setExpirationTime(calculateExpiry(EXPIRATION_MINUTES));
        tfa.setUserId(authUser.getId());
        return twoFactorOTPRepository.save(tfa);
    }

    @Override
    public TwoFactorAuthentication findByUser(Long userId) {
        return twoFactorOTPRepository.findByUserId(userId);
    }

    @Override
    public TwoFactorAuthentication findById(Long id) {
        return twoFactorOTPRepository.findById(id).orElse(null);
    }

    @Override
    public boolean verifyTwoFactorOtp(TwoFactorAuthentication tfa, String otp) {
        return tfa.getOtp().equals(otp);
    }

    @Override
    public void deleteTwoFactorOtp(TwoFactorAuthentication tfa) {
        twoFactorOTPRepository.delete(tfa);
    }

    @Override
    public ResponseEntity<?> findByToken(String token) {
        TwoFactorAuthentication tfa = twoFactorOTPRepository.findByJwt(token)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "2FA session token not found", ErrorCode.TOKEN_INVALID));

        if (new Date().after(tfa.getExpirationTime())) {
            throw new BadRequestException("2FA session has expired. Please log in again.",
                    ErrorCode.OTP_EXPIRED);
        }

        User user = userRepository.findById(tfa.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User associated with this token not found", ErrorCode.USER_NOT_FOUND));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("status",  HttpStatus.OK.value());
        body.put("email",   user.getEmail());
        return ResponseEntity.ok(body);
    }

    @Override
    public ResponseEntity<?> verifyUserTwoFactorOtp(OTPRequest request) {
        TwoFactorAuthentication tfa = twoFactorOTPRepository.findByOTP(request.getOtp())
                .orElseThrow(() -> new BadRequestException(
                        "Invalid OTP. Please check your email and try again.", ErrorCode.INVALID_OTP));
        if (new Date().after(tfa.getExpirationTime())) {
            twoFactorOTPRepository.delete(tfa);
            throw new BadRequestException(
                    "OTP has expired. Please log in again to receive a new code.", ErrorCode.OTP_EXPIRED);
        }

        User user = userRepository.findById(tfa.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found", ErrorCode.USER_NOT_FOUND));
        User principal = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found", ErrorCode.USER_NOT_FOUND));

        UserTracer session = userTracerService.createSession(principal);
        String tokenId = UUID.randomUUID().toString();
        String jwtToken = jwtService.generateToken(
                (UserDetails) principal,
                session.getSessionId(),
                tokenId,
                List.of("PASSWORD", "MFA")
        );

        Optional<UserRecord> record = userRecordRepository.findByUserId(user.getId());

        twoFactorOTPRepository.delete(tfa);

        log.info("[2FA] OTP verified successfully for userId={}", user.getId());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success",             true);
        body.put("status",              HttpStatus.OK.value());
        body.put("jwt",                 jwtToken);
        body.put("userId",              user.getId());
        body.put("email",               user.getEmail());
        body.put("username",            user.getUsername());
        body.put("fullname",            record.map(r -> r.getFirstName() + " " + r.getLastName()).orElse(""));
        body.put("date_of_birth",       record.map(UserRecord::getDateOfBirth).orElse(null));
        body.put("referral_code",       record.map(UserRecord::getReferralCode).orElse(null));
        body.put("referred_by_code",    record.map(UserRecord::getReferredByCode).orElse(null));
        body.put("country",             record.map(UserRecord::getCountry).orElse(null));
        body.put("state",               record.map(UserRecord::getState).orElse(null));
        body.put("city",                record.map(UserRecord::getCity).orElse(null));
        body.put("gender",              record.map(UserRecord::getGender).orElse(null));
        body.put("telephone",           record.map(UserRecord::getPhoneNumber).orElse(null));
        body.put("is_verified",         user.isEnabled());
        body.put("is_profile_complete", record.map(UserRecord::isProfileComplete).orElse(false));
        body.put("two_factor_enabled",  user.isTwoFactorEnabled());
        body.put("session",             true);
        return ResponseEntity.ok(body);
    }

    @Override
    public ResponseEntity<?> enableTwoFactorKey(Boolean enable2fa, Authentication authentication) {
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Authenticated user not found", ErrorCode.USER_NOT_FOUND));

        user.setTwoFactorEnabled(enable2fa);
        userRepository.save(user);

        final String eventTime = ZonedDateTime.now(ZoneId.systemDefault()).format(EVT_FMT);
        final String eventType = Boolean.TRUE.equals(enable2fa) ? "TWO_FACTOR_ENABLED" : "TWO_FACTOR_DISABLED";

        userRecordRepository.findByUserId(user.getId()).ifPresent(rec -> {
            final String fullName = rec.getFirstName() + " " + rec.getLastName();
            CompletableFuture.runAsync(() ->
                notificationPublisher.publishAccountSecurityAlert(
                    user.getEmail(), fullName, user.getUsername(),
                    eventType, eventTime,
                    null, null, null, null
                )
            ).exceptionally(ex -> {
                log.warn("[2FAToggle] Notification failed for userId={}: {}", user.getId(), ex.getMessage());
                return null;
            });
        });

        log.info("[2FA] 2FA {} for userId={}", enable2fa ? "enabled" : "disabled", user.getId());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("status",  HttpStatus.OK.value());
        body.put("message", "Two-factor authentication has been " + (enable2fa ? "enabled" : "disabled") + " successfully.");
        return ResponseEntity.ok(body);
    }

    private Date calculateExpiry(int minutes) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, minutes);
        return cal.getTime();
    }
}
