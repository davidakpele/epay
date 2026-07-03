package com.epay.domain.auth.response;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String jwt;
    private boolean status;
    private String message;
    private boolean isTwoFactorAuthEnabled;
    private String session;
    private String verifylink;


    public AuthResponse() {
    }

    public AuthResponse(String jwt, boolean status, String message, boolean isTwoFactorAuthEnabled, String session, String verifylink) {
        this.jwt = jwt;
        this.status = status;
        this.message = message;
        this.isTwoFactorAuthEnabled = isTwoFactorAuthEnabled;
        this.session = session;
        this.verifylink = verifylink;
    }


    public String getJwt() {
        return this.jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    public boolean isStatus() {
        return this.status;
    }

    public boolean getStatus() {
        return this.status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isIsTwoFactorAuthEnabled() {
        return this.isTwoFactorAuthEnabled;
    }

    public boolean getIsTwoFactorAuthEnabled() {
        return this.isTwoFactorAuthEnabled;
    }

    public void setIsTwoFactorAuthEnabled(boolean isTwoFactorAuthEnabled) {
        this.isTwoFactorAuthEnabled = isTwoFactorAuthEnabled;
    }

    public String getSession() {
        return this.session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getVerifylink() {
        return this.verifylink;
    }

    public void setVerifylink(String verifylink) {
        this.verifylink = verifylink;
    }

}
