package com.example.auth_user_service.interfaces;

import com.example.auth_user_service.enums.ContactMethod;

public interface IMessagingService {
    void sendSmSMessage(String recipient);
    void sendWhatsAppMessage(String recipient);
    void sendEmailMessage(String email);
    void invalidateOTP(String identifier);
    boolean verifyOTP(String identifier, String otpToVerify);
    void sendWelcomeMessage(String recipient, String username, ContactMethod method);
}
