package com.example.auth_user_service.payloads;

public class NotificationUpdateRequest {
    private Boolean email;
    private Boolean transactionAlerts;
    private Boolean loginAlerts;
    private Boolean marketingEmails;
    private Boolean sms;

    public Boolean getEmail() { return email; }
    public void setEmail(Boolean email) { this.email = email; }

    public Boolean getTransactionAlerts() { return transactionAlerts; }
    public void setTransactionAlerts(Boolean transactionAlerts) { this.transactionAlerts = transactionAlerts; }

    public Boolean getLoginAlerts() { return loginAlerts; }
    public void setLoginAlerts(Boolean loginAlerts) { this.loginAlerts = loginAlerts; }

    public Boolean getMarketingEmails() { return marketingEmails; }
    public void setMarketingEmails(Boolean marketingEmails) { this.marketingEmails = marketingEmails; }

    public Boolean getSms() { return sms; }
    public void setSms(Boolean sms) { this.sms = sms; }
}
