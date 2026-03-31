package com.example.auth_user_service.interfaces;

public interface ISmsService {
    void sendVerificationCode(String toPhoneNumber, String messageText);
}
