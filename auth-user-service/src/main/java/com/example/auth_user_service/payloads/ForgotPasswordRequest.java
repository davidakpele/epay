package com.example.auth_user_service.payloads;


public class ForgotPasswordRequest {

    private String identifier;

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
