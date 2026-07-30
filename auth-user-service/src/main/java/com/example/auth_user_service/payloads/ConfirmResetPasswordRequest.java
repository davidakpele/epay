package com.example.auth_user_service.payloads;


public class ConfirmResetPasswordRequest {

    private String identifier;

    private String otp;

    private String newPassword;

    public ConfirmResetPasswordRequest() {}

    public ConfirmResetPasswordRequest(String identifier, String otp, String newPassword) {
        this.identifier  = identifier;
        this.otp         = otp;
        this.newPassword = newPassword;
    }

    public String getIdentifier()              { return identifier; }
    public void   setIdentifier(String v)      { this.identifier = v; }

    public String getOtp()                     { return otp; }
    public void   setOtp(String v)             { this.otp = v; }

    public String getNewPassword()             { return newPassword; }
    public void   setNewPassword(String v)     { this.newPassword = v; }
}
