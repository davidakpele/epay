package com.example.auth_user_service.payloads;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChangePasswordRequest {
    private String password;
    private String confirmPassword;
    private String oldPassword;
    private String token;

    public ChangePasswordRequest() {
    }

    public ChangePasswordRequest(String password, String confirmPassword, String oldPassword, String token) {
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.oldPassword = oldPassword;
        this.token = token;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return this.confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getOldPassword() {
        return this.oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
