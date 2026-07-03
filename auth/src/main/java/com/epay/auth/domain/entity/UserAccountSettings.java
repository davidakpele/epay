package com.epay.auth.domain.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "user_account_settings")
public class UserAccountSettings {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private Users user;
    
    @Column(name = "is_biometric")
    private boolean isBiometric;
    
    @Column(name = "session_timeout")
    private String sessionTimeOut;
    
    @Column(name = "is_email_alert")
    private boolean isEmailAlert;
    
    @Column(name = "is_transaction_alert")
    private boolean isTransactionAlert;
    
    @Column(name = "is_login_alert")
    private boolean isLoginAlert;
    
    @Column(name = "is_receive_marketing_news")
    private boolean isReceiveMarketingNews;
    
    @Column(name = "is_receive_sms_message")
    private boolean isReceiveSmsMessage;
    
    @Column(name = "preferred_language")
    private String preferredLanguage;
    
    @Column(name = "time_zone")
    private String timeZone;

    public UserAccountSettings() {
    }

    public UserAccountSettings(Long id, Users user, boolean isBiometric, String sessionTimeOut, boolean isEmailAlert, boolean isTransactionAlert, boolean isLoginAlert, boolean isReceiveMarketingNews, boolean isReceiveSmsMessage, String preferredLanguage, String timeZone) {
        this.id = id;
        this.user = user;
        this.isBiometric = isBiometric;
        this.sessionTimeOut = sessionTimeOut;
        this.isEmailAlert = isEmailAlert;
        this.isTransactionAlert = isTransactionAlert;
        this.isLoginAlert = isLoginAlert;
        this.isReceiveMarketingNews = isReceiveMarketingNews;
        this.isReceiveSmsMessage = isReceiveSmsMessage;
        this.preferredLanguage = preferredLanguage;
        this.timeZone = timeZone;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Users getUser() {
        return this.user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public boolean isIsBiometric() {
        return this.isBiometric;
    }

    public boolean getIsBiometric() {
        return this.isBiometric;
    }

    public void setIsBiometric(boolean isBiometric) {
        this.isBiometric = isBiometric;
    }

    public String getSessionTimeOut() {
        return this.sessionTimeOut;
    }

    public void setSessionTimeOut(String sessionTimeOut) {
        this.sessionTimeOut = sessionTimeOut;
    }

    public boolean isIsEmailAlert() {
        return this.isEmailAlert;
    }

    public boolean getIsEmailAlert() {
        return this.isEmailAlert;
    }

    public void setIsEmailAlert(boolean isEmailAlert) {
        this.isEmailAlert = isEmailAlert;
    }

    public boolean isIsTransactionAlert() {
        return this.isTransactionAlert;
    }

    public boolean getIsTransactionAlert() {
        return this.isTransactionAlert;
    }

    public void setIsTransactionAlert(boolean isTransactionAlert) {
        this.isTransactionAlert = isTransactionAlert;
    }

    public boolean isIsLoginAlert() {
        return this.isLoginAlert;
    }

    public boolean getIsLoginAlert() {
        return this.isLoginAlert;
    }

    public void setIsLoginAlert(boolean isLoginAlert) {
        this.isLoginAlert = isLoginAlert;
    }

    public boolean isIsReceiveMarketingNews() {
        return this.isReceiveMarketingNews;
    }

    public boolean getIsReceiveMarketingNews() {
        return this.isReceiveMarketingNews;
    }

    public void setIsReceiveMarketingNews(boolean isReceiveMarketingNews) {
        this.isReceiveMarketingNews = isReceiveMarketingNews;
    }

    public boolean isIsReceiveSmsMessage() {
        return this.isReceiveSmsMessage;
    }

    public boolean getIsReceiveSmsMessage() {
        return this.isReceiveSmsMessage;
    }

    public void setIsReceiveSmsMessage(boolean isReceiveSmsMessage) {
        this.isReceiveSmsMessage = isReceiveSmsMessage;
    }

    public String getPreferredLanguage() {
        return this.preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public String getTimeZone() {
        return this.timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }
    
}