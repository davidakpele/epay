package com.example.auth_user_service.services;

import com.example.auth_user_service.interfaces.IJwtService;
import com.example.auth_user_service.interfaces.ITwoFactorAuthenticationService;
import com.example.auth_user_service.models.TwoFactorAuthentication;
import com.example.auth_user_service.models.UserRecord;
import com.example.auth_user_service.models.Users;
import com.example.auth_user_service.payloads.OTPRequest;
import com.example.auth_user_service.exceptions.Error;
import com.example.auth_user_service.repositories.TwoFactorOTPRepository;
import com.example.auth_user_service.repositories.UsersRepository;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class TwoFactorAuthenticationService implements ITwoFactorAuthenticationService{
    private final TwoFactorOTPRepository twoFactorOTPRepository;
    private static final int EXPIRATION_MINUTES = 1;
    private final UsersRepository userRepository;
    private final IJwtService jwtService;

    public TwoFactorAuthenticationService(TwoFactorOTPRepository twoFactorOTPRepository,
            UsersRepository userRepository, IJwtService jwtService) {
        this.twoFactorOTPRepository = twoFactorOTPRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Override
    public TwoFactorAuthentication createTwoFactorOtp(Users authUser, String otp, String jwtToken) {
        Date expirationTime = calculateExpirationDate(EXPIRATION_MINUTES);
        TwoFactorAuthentication twoFactorOTP = new TwoFactorAuthentication();
        twoFactorOTP.setOtp(otp);
        twoFactorOTP.setToken(jwtToken);
        twoFactorOTP.setExpirationTime(expirationTime);
        twoFactorOTP.setUserId(authUser.getId());

        return twoFactorOTPRepository.save(twoFactorOTP);

    }

    @Override
    public TwoFactorAuthentication findByUser(Long userid) {
        return twoFactorOTPRepository.findByUserId(userid);
    }

    @Override
    public TwoFactorAuthentication findById(Long id) {
        Optional<TwoFactorAuthentication> opt = twoFactorOTPRepository.findById(id);
        return opt.orElse(null);
    }

    @Override
    public boolean verifyTwoFactorOtp(TwoFactorAuthentication twoFactorOTP, String otp) {
        return twoFactorOTP.getOtp().equals(otp);
    }

    @Override
    public void deleteTwoFactorOtp(TwoFactorAuthentication twoFactorOTP) {
        twoFactorOTPRepository.delete(twoFactorOTP);
    }

    @Override
    public ResponseEntity<?> findByToken(String token) {
        Optional<TwoFactorAuthentication> checkExists = twoFactorOTPRepository.findByJwt(token);
        Map<String, Object> response = new HashMap<>();

        if (checkExists.isPresent()) {
            TwoFactorAuthentication twoFactorOTP = checkExists.get();
            Date currentDate = new Date();
            if (currentDate.after(twoFactorOTP.getExpirationTime())) {
                response.put("status", false);
                response.put("message", "Token has expired for verification session. please wait you will redirect to login");
            } else {
                Optional<Users> user = userRepository.findById(twoFactorOTP.getUserId());
                if (user.isPresent()) {
                    response.put("status", true);
                    response.put("email", user.get().getEmail());
                } else {
                    response.put("status", false);
                    response.put("email", "");
                }
            }
        } else {
            response.put("status", false);
            response.put("message", "Token not found");
        }

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<?> verifyUserTwoFactorOtp(OTPRequest reqOtpPayload) {
        Optional<TwoFactorAuthentication> verifyExistingOTP = twoFactorOTPRepository.findByOTP(reqOtpPayload.getOtp());
        Map<String, Object> response = new HashMap<>();

        Date currentDate = new Date();

        if (verifyExistingOTP.isPresent()) {
            if (currentDate.after(verifyExistingOTP.get().getExpirationTime())) {
                response.put("status", false);
                response.put("success", false);
                response.put("message", "Token has expired");

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            Optional<Users> userInfo = userRepository.findById(verifyExistingOTP.get().getUserId());

            if (userInfo.isPresent()) {
                var userjwtInfo = userRepository.findByUsername(userInfo.get().getUsername())
                        .orElseThrow(() -> new AuthenticationServiceException("User not found"));
                var jwtToken = jwtService.generateToken((UserDetails) userjwtInfo, userInfo.get().getId());
                
                Users user = userInfo.get();
                UserRecord record = user.getRecords().get(0);

                response.put("jwt", jwtToken);
                response.put("email", user.getEmail());
                response.put("userId", user.getId());
                response.put("username", user.getUsername());
                response.put("fullname", record.getFirstName() + " " + record.getLastName());
                response.put("date_of_birth", record.getDateofBirth());
                response.put("referral_link", record.getReferralLink());
                response.put("referral_username", record.getReferralUsername());
                response.put("country", record.getCountry());
                response.put("state", record.getState());
                response.put("city", record.getCity());
                response.put("gender", record.getGender());
                response.put("telephone", record.getTelephone());
                response.put("is_verify", user.isEnabled());
                response.put("is_profile_complete", record.isProfileComplete());
                response.put("twoFactorAuthEnabled", user.isTwoFactorAuth());
                response.put("success", true);
                response.put("session", true);
                response.put("status", HttpStatus.OK.value());

                return ResponseEntity.ok(response);
            } else {
                response.put("jwt", null);
                response.put("userId", null);
                response.put("username", null);
                response.put("success", false);
                response.put("message", "No token found");

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } else {
            response.put("jwt", null);
            response.put("userId", null);
            response.put("username", null);
            response.put("success", false);
            response.put("message", "Invalid OTP Provided. Please check your email and try again.");

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @Override
    public ResponseEntity<?> enableTwoFactorKey(Boolean enable2fa, Authentication authentication) {
        String username = authentication.getName();
        Optional<Users> optionalUser = userRepository.findByUsername(username);
        if (!optionalUser.isPresent()) {
            return Error.createResponse(
                    "UNAUTHORIZE ACCESS", HttpStatus.FORBIDDEN,
                    "You dont have access to the endpoints");
        }
        Users user = optionalUser.get();
        user.setTwoFactorAuth(enable2fa);
        userRepository.save(user);
        return ResponseEntity.ok().body("Two-Factor Authentication updated successfully");
    }

    private Date calculateExpirationDate(int expirationMinutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(new Date().getTime());
        calendar.add(Calendar.MINUTE, expirationMinutes);
        return new Date(calendar.getTime().getTime());
    }

}
