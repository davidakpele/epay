package com.example.auth_user_service.payloads;

/**
 * Request body for POST /auth/forgot-username.
 * The frontend sends the user's registered email address;
 * the service looks up their username and emails it back.
 */
public class ForgotUsernameRequest {

    private String email;

    public ForgotUsernameRequest() {}

    public ForgotUsernameRequest(String email) {
        this.email = email;
    }

    public String getEmail()          { return email; }
    public void   setEmail(String v)  { this.email = v; }
}
