package pesco.example.authentication_service.payloads;

public class SessionTimeoutUpdateRequest {
    private String sessionTimeout;

    public String getSessionTimeout() { return sessionTimeout; }
    public void setSessionTimeout(String sessionTimeout) { this.sessionTimeout = sessionTimeout; }
}