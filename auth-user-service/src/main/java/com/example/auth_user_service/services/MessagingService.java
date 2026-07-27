package com.example.auth_user_service.services;

import com.example.auth_user_service.dtos.OTPData;
import com.example.auth_user_service.enums.ContactMethod;
import com.example.auth_user_service.enums.OTPType;
import com.example.auth_user_service.interfaces.IMessagingService;
import com.example.auth_user_service.interfaces.INotificationServiceClient;
import com.example.auth_user_service.interfaces.ISmsService;
import com.example.auth_user_service.interfaces.IWhatsAppService;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class MessagingService implements IMessagingService{

    private final INotificationServiceClient emailService;
    private final ISmsService smsService;    
    private final IWhatsAppService whatsAppService;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final ConcurrentHashMap<String, OTPData> OTP_STORE = new ConcurrentHashMap<>();

    private static final int DEFAULT_OTP_LENGTH = 6;
    private static final long DEFAULT_OTP_EXPIRY_MINUTES = 15;
    private static final String NUMERIC = "0123456789";
    private static final String ALPHANUMERIC = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String ALPHANUMERIC_LOWER = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public MessagingService(INotificationServiceClient emailService, ISmsService smsService, IWhatsAppService whatsAppService) {
        this.emailService = emailService;
        this.smsService = smsService;
        this.whatsAppService = whatsAppService;
    }
    
    public static String normalizeIdentifier(String identifier) {
        if (identifier == null) return null;
        String trimmed = identifier.trim();
        
        if (trimmed.matches("^\\+?\\d+$")) {
            if (!trimmed.startsWith("+")) {
                return "+" + trimmed;
            }
            return trimmed;
        }
        
        return trimmed.toLowerCase();
    }
 
    @Override
    public void sendSmSMessage(String recipient){
        String otp = generateAndStoreOTP(
            recipient, 
            OTPType.NUMERIC, 
            DEFAULT_OTP_LENGTH, 
            DEFAULT_OTP_EXPIRY_MINUTES
        );
        String message = String.format("Your verification code is: %s. Valid for %d minutes.",  otp, DEFAULT_OTP_EXPIRY_MINUTES);
        
        smsService.sendVerificationCode(recipient, message);
    }

    @Override
    public void sendWhatsAppMessage(String recipient){
         String otp = generateAndStoreOTP(
            recipient,
            OTPType.NUMERIC,
            DEFAULT_OTP_LENGTH,
            DEFAULT_OTP_EXPIRY_MINUTES
        );
        String message = String.format("🔐 *Verification Code*\n\nYour code is: *%s*\n\nThis code will expire in %d minutes.", otp, DEFAULT_OTP_EXPIRY_MINUTES);
        whatsAppService.sendVerificationCode(recipient, message);
    }
    
    @Override
    public void sendEmailMessage(String email){
        String otp = generateAndStoreOTP(
            email,
            OTPType.NUMERIC,
            DEFAULT_OTP_LENGTH,
            DEFAULT_OTP_EXPIRY_MINUTES
        );
        String content = otp;
    
       emailService.sendRegistrationOTPMessage(email, content);
    }
   
    @Override
    public void sendWelcomeMessage(String recipient, String username, ContactMethod method) {
        String message = "Welcome " + username + "! 🎉\n\n"
                + "Your account has been successfully registered and verified.\n"
                + "Your wallet has also been created and is ready for use.\n\n"
                + "You can now start sending, receiving, and managing your funds seamlessly.";
        switch (method) {
            case EMAIL -> emailService.sendWelcomeEmail(recipient, username, message);
            case SMS -> smsService.sendVerificationCode(recipient, message);
            case WHATSAPP -> whatsAppService.sendCustomMessage(recipient, message);
        }
    }

    public static String generateNumericOTP(int length) {
        StringBuilder otp = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            otp.append(NUMERIC.charAt(RANDOM.nextInt(NUMERIC.length())));
        }
        return otp.toString();
    }

     public static String generateAlphanumericOTP(int length, boolean includeLowerCase) {
        String characters = includeLowerCase ? ALPHANUMERIC_LOWER : ALPHANUMERIC;
        StringBuilder otp = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            otp.append(characters.charAt(RANDOM.nextInt(characters.length())));
        }
        return otp.toString();
    }

    public static String generateOTP(OTPType type, int length) {
        return switch (type) {
            case NUMERIC -> generateNumericOTP(length);
            case ALPHANUMERIC -> generateAlphanumericOTP(length, false);
            case ALPHANUMERIC_LOWER -> generateAlphanumericOTP(length, true);
        };
    }

    /**
     * Generate and store OTP with expiration
     */
    public static String generateAndStoreOTP(String identifier, OTPType type, int length, long expiryMinutes) {
        String normalizedIdentifier = normalizeIdentifier(identifier);
        String otp = generateOTP(type, length);
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(expiryMinutes);
        
        OTP_STORE.put(normalizedIdentifier, new OTPData(otp, expiryTime, normalizedIdentifier, false));
        
        if (RANDOM.nextInt(100) < 5) {
            cleanupExpiredOTPs();
        }
        
        return otp;
    }

    public static String generateAndStoreOTP(String identifier) {
        return generateAndStoreOTP(identifier, OTPType.NUMERIC, DEFAULT_OTP_LENGTH, DEFAULT_OTP_EXPIRY_MINUTES);
    }
    
    @Override
    public boolean verifyOTP(String identifier, String otpToVerify) {
        String normalizedIdentifier = normalizeIdentifier(identifier);
        String trimmedOTP = otpToVerify != null ? otpToVerify.trim() : null;
      
        OTPData otpData = OTP_STORE.get(normalizedIdentifier);
        
        if (otpData == null) {
            return false;
        }
        
        if (!otpData.isValid()) {
            return false;
        }
        
        boolean matches = otpData.getOtp().equals(trimmedOTP);
    
        if (matches) {
            otpData.setUsed(true);
            return true;
        }
        
        return false;
    }
    
    public static boolean isOTPValid(String identifier) {
        String normalizedIdentifier = normalizeIdentifier(identifier);
        OTPData otpData = OTP_STORE.get(normalizedIdentifier);
        boolean isValid = otpData != null && otpData.isValid();
        return isValid;
    }


    @Override
    public void invalidateOTP(String identifier) {
        String normalizedIdentifier = normalizeIdentifier(identifier);
        OTP_STORE.remove(normalizedIdentifier);
    }

    public static long getRemainingTime(String identifier) {
        String normalizedIdentifier = normalizeIdentifier(identifier);
        OTPData otpData = OTP_STORE.get(normalizedIdentifier);
        
        if (otpData == null) {
            return 0;
        }
        
        if (otpData.isExpired()) {
            return 0;
        }
        
        long minutes = java.time.Duration.between(LocalDateTime.now(), otpData.getExpiryTime()).toMinutes();
        return minutes;
    }
    
    private static void cleanupExpiredOTPs() {
        OTP_STORE.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    public static int getOTPStoreSize() {
        return OTP_STORE.size();
    }
    
   
}