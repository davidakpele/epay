package com.epay.auth.service;

import com.epay.auth.interfaces.IMessagingService;
import com.epay.common.interfaces.IAuthNotificationPublisher;
import com.epay.domain.auth.dto.OTPData;
import com.epay.domain.auth.enums.ContactMethod;
import com.epay.domain.auth.enums.OTPType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessagingService implements IMessagingService {

    private static final Duration OTP_TTL                 = Duration.ofMinutes(10);
    private static final String   OTP_PREFIX              = "otp:";
    private static final int      DEFAULT_OTP_LENGTH      = 6;
    private static final long     DEFAULT_OTP_EXPIRY_MIN  = 10;
    private static final SecureRandom RANDOM              = new SecureRandom();

    private static final String NUMERIC          = "0123456789";
    private static final String ALPHANUMERIC     = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String ALPHANUMERIC_LOW = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final ConcurrentHashMap<String, OTPData> OTP_STORE = new ConcurrentHashMap<>();

    private final RedisTemplate<String, Object>  redisTemplate;
    private final IAuthNotificationPublisher     notificationPublisher;

    @Override
    public void sendSmSMessage(String phoneNumber) {
        String otp = generateAndStore(phoneNumber, OTPType.NUMERIC,
                DEFAULT_OTP_LENGTH, DEFAULT_OTP_EXPIRY_MIN);

        CompletableFuture.runAsync(() -> {
            try {
                notificationPublisher.publishNewUserOTPVerification(phoneNumber, otp);
                log.info("[Messaging] SMS OTP dispatched for identifier={}", mask(phoneNumber));
            } catch (Exception e) {
                log.warn("[Messaging] SMS notification failed for identifier={}: {}",
                        mask(phoneNumber), e.getMessage());
            }
        });
    }

    @Override
    public void sendWhatsAppMessage(String phoneNumber) {
        String otp = generateAndStore(phoneNumber, OTPType.NUMERIC,
                DEFAULT_OTP_LENGTH, DEFAULT_OTP_EXPIRY_MIN);

        CompletableFuture.runAsync(() -> {
            try {
                notificationPublisher.publishNewUserOTPVerification(phoneNumber, otp);
                log.info("[Messaging] WhatsApp OTP dispatched for identifier={}", mask(phoneNumber));
            } catch (Exception e) {
                log.warn("[Messaging] WhatsApp notification failed for identifier={}: {}",
                        mask(phoneNumber), e.getMessage());
            }
        });
    }

    @Override
    public void sendEmailMessage(String email) {
        String otp = generateAndStore(email, OTPType.NUMERIC,
                DEFAULT_OTP_LENGTH, DEFAULT_OTP_EXPIRY_MIN);

        CompletableFuture.runAsync(() -> {
            try {
                notificationPublisher.publishNewUserOTPVerification(email, otp);
                log.info("[Messaging] Email OTP dispatched for email={}", mask(email));
            } catch (Exception e) {
                log.warn("[Messaging] Email OTP notification failed for email={}: {}",
                        mask(email), e.getMessage());
            }
        });
    }

    @Override
    public void sendWelcomeMessage(String recipient, String username, ContactMethod method) {
        CompletableFuture.runAsync(() -> {
            try {
                switch (method) {
                    case EMAIL -> notificationPublisher.publishVerificationEmail(
                            recipient,
                            "Welcome to ePay, " + username + "! Your account has been created successfully.",
                            null,
                            username);
                    case SMS, WHATSAPP -> notificationPublisher.publishNewUserOTPVerification(
                            recipient, username);
                    default -> log.warn("[Messaging] Unknown contact method={} for welcome message", method);
                }
                log.info("[Messaging] Welcome message dispatched to={} via={}", mask(recipient), method);
            } catch (Exception e) {
                log.warn("[Messaging] Welcome notification failed for recipient={}: {}",
                        mask(recipient), e.getMessage());
            }
        });
    }

    @Override
    public boolean verifyOTP(String identifier, String otpToVerify) {
        String key = normalizeIdentifier(identifier);
        try {
            Object stored = redisTemplate.opsForValue().get(OTP_PREFIX + key);
            if (stored != null) {
                boolean valid = stored.toString().equals(otpToVerify);
                if (valid) invalidateOTP(identifier);
                return valid;
            }
            OTPData data = OTP_STORE.get(key);
            if (data != null && data.isValid() && data.getOtp().equals(otpToVerify)) {
                data.setUsed(true);
                invalidateOTP(identifier);
                return true;
            }
            log.debug("[Messaging] No valid OTP found for identifier={}", mask(identifier));
            return false;
        } catch (Exception e) {
            log.warn("[Messaging] Redis verify failed for identifier={}: {}",
                    mask(identifier), e.getMessage());
            return false;
        }
    }

    @Override
    public void invalidateOTP(String identifier) {
        String key = normalizeIdentifier(identifier);
        try {
            redisTemplate.delete(OTP_PREFIX + key);
        } catch (Exception e) {
            log.warn("[Messaging] Redis delete failed for identifier={}: {}",
                    mask(identifier), e.getMessage());
        }
        OTP_STORE.remove(key);
    }

    public String generateAndStore(String identifier, OTPType type, int length, long expiryMinutes) {
        String key = normalizeIdentifier(identifier);
        String otp = generateOTP(type, length);
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(expiryMinutes);
        try {
            redisTemplate.opsForValue().set(OTP_PREFIX + key, otp, OTP_TTL);
        } catch (Exception e) {
            log.warn("[Messaging] Redis store failed for identifier={}: {}",
                    mask(identifier), e.getMessage());
        }

        OTP_STORE.put(key, new OTPData(otp, expiry, key, false));

        if (RANDOM.nextInt(100) < 5) {
            OTP_STORE.entrySet().removeIf(entry -> entry.getValue().isExpired());
        }

        log.debug("[Messaging] OTP generated and stored for identifier={}", mask(identifier));
        return otp;
    }

    public String generateAndStoreDefault(String identifier) {
        return generateAndStore(identifier, OTPType.NUMERIC,
                DEFAULT_OTP_LENGTH, DEFAULT_OTP_EXPIRY_MIN);
    }

    public static String generateOTP(OTPType type, int length) {
        return switch (type) {
            case NUMERIC          -> generateNumericOTP(length);
            case ALPHANUMERIC     -> generateAlphanumericOTP(length, false);
            case ALPHANUMERIC_LOWER -> generateAlphanumericOTP(length, true);
        };
    }

    private static String generateNumericOTP(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(NUMERIC.charAt(RANDOM.nextInt(NUMERIC.length())));
        }
        return sb.toString();
    }

    private static String generateAlphanumericOTP(int length, boolean lowercase) {
        String chars = lowercase ? ALPHANUMERIC_LOW : ALPHANUMERIC;
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public static String normalizeIdentifier(String identifier) {
        if (identifier == null) return null;
        String trimmed = identifier.trim();
        if (trimmed.matches("^\\+?\\d{7,15}$")) {
            return trimmed.startsWith("+") ? trimmed : "+" + trimmed;
        }
        return trimmed.toLowerCase();
    }

    private static String mask(String value) {
        if (value == null || value.length() < 4) return "***";
        return value.substring(0, 3) + "***";
    }
}
