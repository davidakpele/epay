package com.example.auth_user_service.payloads;

/**
 * Request body for POST /auth/forgot-password.
 * The frontend sends the user's email or phone together with
 * the contact method so the service can generate and dispatch an OTP.
 */
public class ForgotPasswordRequest {

    /** The user's registered email address OR phone number */
    private String identifier;

    /** "EMAIL" or "PHONE" */
    private String method;

    public ForgotPasswordRequest() {}

    public ForgotPasswordRequest(String identifier, String method) {
        this.identifier = identifier;
        this.method     = method;
    }

    public String getIdentifier()              { return identifier; }
    public void   setIdentifier(String v)      { this.identifier = v; }

    public String getMethod()                  { return method; }
    public void   setMethod(String v)          { this.method = v; }
}
