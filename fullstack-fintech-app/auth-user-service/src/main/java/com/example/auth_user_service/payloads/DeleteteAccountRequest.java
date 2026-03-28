package com.example.auth_user_service.payloads;

public class DeleteteAccountRequest {
    private String reason;
    private String username;
    private Long userId;
    private String email;
    private String action;

    public DeleteteAccountRequest() {
    }


    public DeleteteAccountRequest(String reason, String username, Long userId, String email, String action) {
        this.reason = reason;
        this.username = username;
        this.userId = userId;
        this.email = email;
        this.action = action;
    }

    public String getReason() {
        return this.reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAction() {
        return this.action;
    }

    public void setAction(String action) {
        this.action = action;
    }

}
