package com.example.auth_user_service.payloads;

public class ForgotUsernameRequest {

    private String email;

    public ForgotUsernameRequest() {}

    public ForgotUsernameRequest(String email) {
        this.email = email;
    }

    public String getEmail()          { return email; }
    public void   setEmail(String v)  { this.email = v; }
}
