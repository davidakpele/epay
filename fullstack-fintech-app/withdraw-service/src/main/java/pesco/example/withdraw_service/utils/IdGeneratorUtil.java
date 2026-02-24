package pesco.example.withdraw_service.utils;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.time.Instant;

import jakarta.servlet.http.HttpServletRequest;

public class IdGeneratorUtil {
    
    // Generate UUID for id field
    public static String generateId() {
        return UUID.randomUUID().toString();
    }
    
    // Generate session ID (32 character alphanumeric)
    public static String generateSessionId() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sessionId = new StringBuilder(32);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        
        for (int i = 0; i < 32; i++) {
            sessionId.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sessionId.toString();
    }
    
    // Generate transaction ID (format: NX + 9 digits)
    public static String generateTransactionId() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        long number = 100000000L + random.nextLong(900000000L);
        return "NX" + number;
    }
    
    // Generate terminal ID (format: TERM + 4 digits)
    public static String generateTerminalId() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int number = 1000 + random.nextInt(9000);
        return "TERM" + number;
    }

    // Generate ER ID (format: ER- + 4 digits)
    public static String generateErId() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int number = 1000 + random.nextInt(9000); 
        return "ER-" + number;
    }
    
    // Generate reference number (format: REF + 8 alphanumeric)
    public static String generateReferenceNo() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder refNo = new StringBuilder("REF");
        ThreadLocalRandom random = ThreadLocalRandom.current();
        
        for (int i = 0; i < 8; i++) {
            refNo.append(characters.charAt(random.nextInt(characters.length())));
        }
        return refNo.toString();
    }
    
    // Get current timestamp in ISO format - FIXED
    public static String getCurrentTimestamp() {
        return Instant.now().toString();
    }
    
    // Get client IP address
    public static String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        
        return ipAddress;
    }
}