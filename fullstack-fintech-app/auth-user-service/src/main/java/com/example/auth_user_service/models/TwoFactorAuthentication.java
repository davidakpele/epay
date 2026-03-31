package com.example.auth_user_service.models;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Builder
@Entity
public class TwoFactorAuthentication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String otp;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Long userId;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String token;

    private Date expirationTime;

    public TwoFactorAuthentication() {
    }


    public TwoFactorAuthentication(Long id, String otp, Long userId, String token, Date expirationTime) {
        this.id = id;
        this.otp = otp;
        this.userId = userId;
        this.token = token;
        this.expirationTime = expirationTime;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOtp() {
        return this.otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getExpirationTime() {
        return this.expirationTime;
    }

    public void setExpirationTime(Date expirationTime) {
        this.expirationTime = expirationTime;
    }


}
