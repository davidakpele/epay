package com.example.auth_user_service.services;

import com.example.auth_user_service.interfaces.ISmsService;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmsService implements ISmsService{
    @Value("${twilio.sms-number:}")
    private String smsNumber;
    
    @Override
    public void sendVerificationCode(String toPhoneNumber, String messageText) {
        if (!toPhoneNumber.startsWith("+")) {
            toPhoneNumber = "+" + toPhoneNumber;
        }
        Message.creator(
            new PhoneNumber(toPhoneNumber),
            new PhoneNumber(smsNumber),
            messageText
        ).create();
    }
}
