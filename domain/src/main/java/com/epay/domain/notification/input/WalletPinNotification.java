package com.epay.domain.notification.input;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WalletPinNotification {

    private String email;
    private String fullName;
    private String username;
    private String action;
    private String actionTime;
    private String ipAddress;
    private String deviceInfo;
    private String supportPhone;
    private String supportEmail;

    public WalletPinNotification() {}

    @JsonCreator
    public WalletPinNotification(
            @JsonProperty("email")        String email,
            @JsonProperty("fullName")     String fullName,
            @JsonProperty("username")     String username,
            @JsonProperty("action")       String action,
            @JsonProperty("actionTime")   String actionTime,
            @JsonProperty("ipAddress")    String ipAddress,
            @JsonProperty("deviceInfo")   String deviceInfo,
            @JsonProperty("supportPhone") String supportPhone,
            @JsonProperty("supportEmail") String supportEmail) {
        this.email        = email;
        this.fullName     = fullName;
        this.username     = username;
        this.action       = action;
        this.actionTime   = actionTime;
        this.ipAddress    = ipAddress;
        this.deviceInfo   = deviceInfo;
        this.supportPhone = supportPhone;
        this.supportEmail = supportEmail;
    }

    public String getEmail()        { return email; }
    public void setEmail(String v)  { this.email = v; }

    public String getFullName()       { return fullName; }
    public void setFullName(String v) { this.fullName = v; }

    public String getUsername()       { return username; }
    public void setUsername(String v) { this.username = v; }

    public String getAction()       { return action; }
    public void setAction(String v) { this.action = v; }

    public String getActionTime()       { return actionTime; }
    public void setActionTime(String v) { this.actionTime = v; }

    public String getIpAddress()       { return ipAddress; }
    public void setIpAddress(String v) { this.ipAddress = v; }

    public String getDeviceInfo()       { return deviceInfo; }
    public void setDeviceInfo(String v) { this.deviceInfo = v; }

    public String getSupportPhone()       { return supportPhone; }
    public void setSupportPhone(String v) { this.supportPhone = v; }

    public String getSupportEmail()       { return supportEmail; }
    public void setSupportEmail(String v) { this.supportEmail = v; }
}
