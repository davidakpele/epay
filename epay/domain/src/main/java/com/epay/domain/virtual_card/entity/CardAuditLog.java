package com.epay.domain.virtual_card.entity;

import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

import com.epay.domain.virtual_card.enums.AuditAction;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Entity
@Table(name = "card_audit_logs", indexes = {
    @Index(name = "idx_card_id", columnList = "card_id"),
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
public class CardAuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "card_id", nullable = false)
    private String cardId;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private AuditAction action;
    
    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue; 
    
    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue; 
    
    @Column(name = "changed_by")
    private String changedBy;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "user_agent")
    private String userAgent;
    
    @Column(name = "reason")
    private String reason;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;


    public CardAuditLog() {
    }

    public CardAuditLog(String id, String cardId, String userId, AuditAction action, String oldValue, String newValue, String changedBy, String ipAddress, String userAgent, String reason, LocalDateTime createdAt) {
        this.id = id;
        this.cardId = cardId;
        this.userId = userId;
        this.action = action;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changedBy = changedBy;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCardId() {
        return this.cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public AuditAction getAction() {
        return this.action;
    }

    public void setAction(AuditAction action) {
        this.action = action;
    }

    public String getOldValue() {
        return this.oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return this.newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getChangedBy() {
        return this.changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public String getIpAddress() {
        return this.ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return this.userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getReason() {
        return this.reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

}
