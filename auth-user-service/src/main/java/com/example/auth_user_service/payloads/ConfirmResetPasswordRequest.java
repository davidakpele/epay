package com.example.auth_user_service.payloads;

/**
 * Request body for POST /auth/reset-password (step 2).
 * User submits the OTP they received plus their chosen new password.
 */
public class ConfirmResetPasswordRequest {

    /** The user's registered email address OR phone number (same value sent in step 1) */
    private String identifier;

    /** The 4-digit OTP the user entered on the form */
    private String otp;

    /** The new password chosen by the user */
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
