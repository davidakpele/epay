package com.epay.common.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "twilio")
public class TwilioProperties {
    private String accountSid;
    private String authToken;
    private String whatsappNumber;
    
    public String getAccountSid() {
        return accountSid;
    }
    
    public void setAccountSid(String accountSid) {
        this.accountSid = accountSid;
    }
    
    public String getAuthToken() {
        return authToken;
    }
    
    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
    
    public String getWhatsappNumber() {
        return whatsappNumber;
    }
    
    public void setWhatsappNumber(String whatsappNumber) {
        this.whatsappNumber = whatsappNumber;
    }
}
