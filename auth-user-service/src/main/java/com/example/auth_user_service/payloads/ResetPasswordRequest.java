package com.example.auth_user_service.payloads;


public class ResetPasswordRequest {
    private String identifier;
    private String method;

    public ResetPasswordRequest() {
    }

    public ResetPasswordRequest(String identifier, String method) {
        this.identifier = identifier;
        this.method = method;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getMethod() {
        return this.method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

}
