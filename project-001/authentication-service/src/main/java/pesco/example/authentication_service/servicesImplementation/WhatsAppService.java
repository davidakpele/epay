package pesco.example.authentication_service.servicesImplementation;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.stereotype.Service;

@Service
public class WhatsAppService {
    
    public static final String ACCOUNT_SID = "AC61c3c30fc7704566a6784eae73d604db";
    public static final String AUTH_TOKEN = "a4bfe859dcdb8de1d4ea225db19d7f72";
    
    public void sendVerificationCode(String toPhoneNumber, String messageText) {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        String normalizedNumber = normalizePhoneNumber(toPhoneNumber);

        System.out.println(
            "WhatsApp message sent to " + normalizedNumber + " with message: " + messageText
        );
        // if (!toPhoneNumber.startsWith("+")) {
        //     toPhoneNumber = "+" + toPhoneNumber;
        // }
        
        Message.creator(
            new PhoneNumber("whatsapp:" + normalizedNumber),
            new PhoneNumber("whatsapp:+14155238886"),
            messageText
        ).create();
    }
    
    public void sendCustomMessage(String toPhoneNumber, String messageText) {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        
        if (!toPhoneNumber.startsWith("+")) {
            toPhoneNumber = "+"+ toPhoneNumber;
        }
        
        Message.creator(
            new PhoneNumber("whatsapp:" + toPhoneNumber),
            new PhoneNumber("whatsapp:+14155238886"),
            messageText
        ).create();
    }

    private String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("Phone number cannot be null or empty");
        }

        // Remove ALL spaces (including leading, trailing, and middle)
        String cleaned = phoneNumber.replaceAll("\\s+", "");

        return cleaned.startsWith("+") ? cleaned : "+" + cleaned;
    }


}