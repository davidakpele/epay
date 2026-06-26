package com.example.auth_user_service.interfaces;

public interface IWhatsAppService {
    void sendVerificationCode(String toPhoneNumber, String messageText);

    void sendCustomMessage(String toPhoneNumber, String messageText);
}
