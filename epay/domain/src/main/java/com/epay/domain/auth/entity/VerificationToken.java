package com.epay.domain.auth.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import java.util.Date;

@Data
@Builder
@Entity
public class VerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "vt_seq")
    @SequenceGenerator(name = "vt_seq", sequenceName = "verification_token_seq", allocationSize = 1)
    private Long id;
    private Long userId;
    private String token;
    private Date expirationTime;


    public VerificationToken() {
    }

    public VerificationToken(Long id, Long userId, String token, Date expirationTime) {
        this.id = id;
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
