package com.epay.common.config.security;

import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TwilioConfig {
    
    private final TwilioProperties twilioProperties;
    
    public TwilioConfig(TwilioProperties twilioProperties) {
        this.twilioProperties = twilioProperties;
    }
    
    @PostConstruct
    public void initTwilio() {
        Twilio.init(twilioProperties.getAccountSid(), twilioProperties.getAuthToken());
    }
}