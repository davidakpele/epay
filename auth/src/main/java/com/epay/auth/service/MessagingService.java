package com.epay.auth.service;

import com.epay.auth.interfaces.IMessagingService;
import com.epay.domain.auth.enums.ContactMethod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

/**
 * OTP lifecycle management backed by Redis.
 *
 * Key pattern:  otp:{identifier}  →  OTP string, TTL = 10 minutes
 *
 * SMS/WhatsApp delivery is delegated to the notification publisher
 * (handled by AuthenticationService which calls this after OTP is stored).
 * This service only manages OTP storage + verification — it does NOT send
 * messages directly to keep the auth module decoupled from Twilio/transport.
 *
 * Fail-open: if Redis is down, verifyOTP returns false (safe default).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessagingService implements IMessagingService {

    private static final Duration OTP_TTL   = Duration.ofMinutes(10);
    private static final String   OTP_PREFIX = "otp:";

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Stores a generated OTP for the given identifier (email or phone).
     * The OTP itself is the recipient value passed in — AuthService generates it.
     */
    @Override
    public void sendSmSMessage(String otp) {
        // Called by AuthService with the OTP value — store it against the current context.
        // Since identifier is not passed here, this method is a no-op store hook.
        // Actual storage happens via storeOtp() called from AuthService.
        log.debug("[Messaging] sendSmSMessage hook called — OTP managed by AuthService");
    }

    @Override
    public void sendWhatsAppMessage(String recipient) {
        log.debug("[Messaging] sendWhatsAppMessage hook called for recipient={}", recipient);
    }

    @Override
    public void sendEmailMessage(String email) {
        log.debug("[Messaging] sendEmailMessage hook called for email={}", email);
    }

    /**
     * Stores an OTP for an identifier so it can be verified later.
     * Public helper used by AuthenticationService.
     */
    public void storeOtp(String identifier, String otp) {
        try {
            redisTemplate.opsForValue().set(OTP_PREFIX + identifier, otp, OTP_TTL);
            log.debug("[Messaging] OTP stored for identifier={}", identifier);
        } catch (Exception e) {
            log.warn("[Messaging] Redis store failed for identifier={}: {}", identifier, e.getMessage());
        }
    }

    @Override
    public boolean verifyOTP(String identifier, String otpToVerify) {
        try {
            Object stored = redisTemplate.opsForValue().get(OTP_PREFIX + identifier);
            if (stored == null) {
                log.debug("[Messaging] No OTP found for identifier={}", identifier);
                return false;
            }
            boolean valid = stored.toString().equals(otpToVerify);
            if (valid) {
                invalidateOTP(identifier);
            }
            return valid;
        } catch (Exception e) {
            log.warn("[Messaging] Redis verify failed for identifier={}: {}", identifier, e.getMessage());
            return false;
        }
    }

    @Override
    public void invalidateOTP(String identifier) {
        try {
            redisTemplate.delete(OTP_PREFIX + identifier);
        } catch (Exception e) {
            log.warn("[Messaging] Redis delete failed for identifier={}: {}", identifier, e.getMessage());
        }
    }

    @Override
    public void sendWelcomeMessage(String recipient, String username, ContactMethod method) {
        // Welcome messages are sent async via notification publisher in AuthenticationService
        log.debug("[Messaging] sendWelcomeMessage hook called recipient={} method={}", recipient, method);
    }

    /** Generates a 6-digit numeric OTP. */
    public static String generateOtp() {
        return String.valueOf((int)(Math.random() * 900_000) + 100_000);
    }
}
