package pesco.example.authentication_service.servicesImplementation;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import pesco.example.authentication_service.clients.NotificationServiceClient;
import pesco.example.authentication_service.dtos.OTPData;
import pesco.example.authentication_service.enums.ContactMethod;
import pesco.example.authentication_service.enums.OTPType;

@Service
public class MessagingService {
    
    private final NotificationServiceClient emailService;
    private final SmsService smsService;    
    private final WhatsAppService whatsAppService;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final ConcurrentHashMap<String, OTPData> OTP_STORE = new ConcurrentHashMap<>();

    private static final int DEFAULT_OTP_LENGTH = 6;
    private static final long DEFAULT_OTP_EXPIRY_MINUTES = 15;
    private static final String NUMERIC = "0123456789";
    private static final String ALPHANUMERIC = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String ALPHANUMERIC_LOWER = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public MessagingService(NotificationServiceClient emailService, SmsService smsService, WhatsAppService whatsAppService) {
        this.emailService = emailService;
        this.smsService = smsService;
        this.whatsAppService = whatsAppService;
    }
    
    // Normalize identifier for consistent storage and retrieval
    public static String normalizeIdentifier(String identifier) {
        if (identifier == null) return null;
        
        String trimmed = identifier.trim();
        
        // For phone numbers (starts with + or is all digits), ensure + is present
        if (trimmed.matches("^\\+?\\d+$")) {
            // It's a phone number
            if (!trimmed.startsWith("+")) {
                return "+" + trimmed;
            }
            return trimmed;
        }
        
        // For emails, lowercase
        return trimmed.toLowerCase();
    }
 
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
    
    public void sendEmailMessage(String email){
        String otp = generateAndStoreOTP(
            email,
            OTPType.NUMERIC,
            DEFAULT_OTP_LENGTH,
            DEFAULT_OTP_EXPIRY_MINUTES
        );
        String content = "Your verification code is: " + otp + ". It is valid for " + DEFAULT_OTP_EXPIRY_MINUTES + " minutes.";
    
       emailService.sendRegistrationOTPMessage(email, content);
    }
   
    public void sendWelcomeMessage(String recipient, String username, ContactMethod method) {
        String message = "Welcome " + username + "! Your account has been verified successfully.";
        
        switch (method) {
            case EMAIL -> emailService.sendWelcomeEmail(recipient, username);
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
        
        System.out.println("=== OTP Generated and Stored ===");
        System.out.println("Original Identifier: " + identifier);
        System.out.println("Normalized Identifier: " + normalizedIdentifier);
        System.out.println("Generated OTP: " + otp);
        System.out.println("Expiry Time: " + expiryTime);
        System.out.println("Current Time: " + LocalDateTime.now());
        System.out.println("Store Size: " + OTP_STORE.size());
        System.out.println("================================");
        
        return otp;
    }
    
    /**
     * Generate and store OTP with default settings
     */
    public static String generateAndStoreOTP(String identifier) {
        return generateAndStoreOTP(identifier, OTPType.NUMERIC, DEFAULT_OTP_LENGTH, DEFAULT_OTP_EXPIRY_MINUTES);
    }
    
    /**
     * Verify OTP
     */
    public static boolean verifyOTP(String identifier, String otpToVerify) {
        String normalizedIdentifier = normalizeIdentifier(identifier);
        String trimmedOTP = otpToVerify != null ? otpToVerify.trim() : null;
        
        System.out.println("=== OTP Verification Attempt ===");
        System.out.println("Original Identifier: " + identifier);
        System.out.println("Normalized Identifier: " + normalizedIdentifier);
        System.out.println("OTP to Verify: " + trimmedOTP);
        System.out.println("Store Size: " + OTP_STORE.size());
        System.out.println("All Keys in Store: " + OTP_STORE.keySet());
        
        OTPData otpData = OTP_STORE.get(normalizedIdentifier);
        
        if (otpData == null) {
            System.out.println("ERROR: OTP Data NOT FOUND for identifier: " + normalizedIdentifier);
            return false;
        }
        
        System.out.println("Stored OTP: " + otpData.getOtp());
        System.out.println("Expiry Time: " + otpData.getExpiryTime());
        System.out.println("Current Time: " + LocalDateTime.now());
        System.out.println("Is Expired: " + otpData.isExpired());
        System.out.println("Is Used: " + otpData.isUsed());
        System.out.println("Is Valid: " + otpData.isValid());
        
        if (!otpData.isValid()) {
            System.out.println("ERROR: OTP is NOT VALID (expired or already used)");
            return false;
        }
        
        boolean matches = otpData.getOtp().equals(trimmedOTP);
        System.out.println("OTP Match: " + matches);
        
        if (matches) {
            otpData.setUsed(true);
            System.out.println("SUCCESS: OTP verified successfully");
            return true;
        }
        
        System.out.println("ERROR: OTP does not match");
        return false;
    }
    
    /**
     * Check if OTP is valid without marking it as used
     */
    public static boolean isOTPValid(String identifier) {
        String normalizedIdentifier = normalizeIdentifier(identifier);
        OTPData otpData = OTP_STORE.get(normalizedIdentifier);
        boolean isValid = otpData != null && otpData.isValid();
        
        System.out.println("=== Checking OTP Validity ===");
        System.out.println("Original Identifier: " + identifier);
        System.out.println("Normalized Identifier: " + normalizedIdentifier);
        System.out.println("OTP Data Found: " + (otpData != null));
        if (otpData != null) {
            System.out.println("Is Valid: " + otpData.isValid());
            System.out.println("Is Expired: " + otpData.isExpired());
            System.out.println("Is Used: " + otpData.isUsed());
        }
        System.out.println("Result: " + isValid);
        
        return isValid;
    }
    
    /**
     * Invalidate/remove OTP
     */
    public static void invalidateOTP(String identifier) {
        String normalizedIdentifier = normalizeIdentifier(identifier);
        System.out.println("Invalidating OTP for: " + normalizedIdentifier);
        OTP_STORE.remove(normalizedIdentifier);
    }
    
    /**
     * Get remaining time for OTP in minutes
     */
    public static long getRemainingTime(String identifier) {
        String normalizedIdentifier = normalizeIdentifier(identifier);
        OTPData otpData = OTP_STORE.get(normalizedIdentifier);
        
        if (otpData == null) {
            System.out.println("No OTP data found for: " + normalizedIdentifier);
            return 0;
        }
        
        if (otpData.isExpired()) {
            System.out.println("OTP expired for: " + normalizedIdentifier);
            return 0;
        }
        
        long minutes = java.time.Duration.between(LocalDateTime.now(), otpData.getExpiryTime()).toMinutes();
        System.out.println("Remaining time for " + normalizedIdentifier + ": " + minutes + " minutes");
        return minutes;
    }
    
    /**
     * Clean up expired OTPs
     */
    private static void cleanupExpiredOTPs() {
        int beforeSize = OTP_STORE.size();
        OTP_STORE.entrySet().removeIf(entry -> entry.getValue().isExpired());
        int afterSize = OTP_STORE.size();
        if (beforeSize != afterSize) {
            System.out.println("Cleaned up " + (beforeSize - afterSize) + " expired OTPs");
        }
    }
    
    /**
     * Get OTP store size (for monitoring)
     */
    public static int getOTPStoreSize() {
        return OTP_STORE.size();
    }
    
   
}