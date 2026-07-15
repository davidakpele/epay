package com.epay.domain.auth.dto;

import java.time.LocalDateTime;

public class OTPData {
    private String otp;
    private LocalDateTime expiryTime;
    private String identifier;
    private boolean used;

    public OTPData() {
    }

    public OTPData(String otp, LocalDateTime expiryTime, String identifier, boolean used) {
        this.otp = otp;
        this.expiryTime = expiryTime;
        this.identifier = identifier;
        this.used = used;
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryTime);
    }
    
    public boolean isValid() {
        return !used && !isExpired();
    }


    public String getOtp() {
        return this.otp;
    }


    public LocalDateTime getExpiryTime() {
        return this.expiryTime;
    }


    public String getIdentifier() {
        return this.identifier;
    }


    public boolean isUsed() {
        return this.used;
    }

    public boolean getUsed() {
        return this.used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

}
