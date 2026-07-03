package com.epay.auth.interfaces;

public interface ISmsService {
    void sendVerificationCode(String toPhoneNumber, String messageText);
}
