package com.epay.domain.notification.input;

public class OTPOnSignUp {

    private String otp;
    private String email;

    public OTPOnSignUp() {
    }

    public OTPOnSignUp(String otp, String email) {
        this.otp = otp;
        this.email = email;
    }

    public String getOtp() {
        return this.otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
