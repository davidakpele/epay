package com.epay.domain.auth.entity;

import java.time.LocalDateTime;
import com.epay.domain.auth.enums.ReportCasesAction;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;
import jakarta.persistence.*;

@Data
@Builder
@Entity
@Table(name = "user_account_cases_report")
public class UserAccountCases {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private Long userId;
    private String email;
    private String reasons;

    @Enumerated(EnumType.STRING)
    private ReportCasesAction action;
    private LocalDateTime createdOn;

    public UserAccountCases() {
    }

    public UserAccountCases(Long id, String username, Long userId, String email, String reasons, ReportCasesAction action, LocalDateTime createdOn) {
        this.id = id;
        this.username = username;
        this.userId = userId;
        this.email = email;
        this.reasons = reasons;
        this.action = action;
        this.createdOn = createdOn;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getReasons() {
        return this.reasons;
    }

    public void setReasons(String reasons) {
        this.reasons = reasons;
    }

    public ReportCasesAction getAction() {
        return this.action;
    }

    public void setAction(ReportCasesAction action) {
        this.action = action;
    }

    public LocalDateTime getCreatedOn() {
        return this.createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

}
