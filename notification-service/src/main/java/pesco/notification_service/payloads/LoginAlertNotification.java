package pesco.notification_service.payloads;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LoginAlertNotification {

    private String email;
    private String fullName;
    private String username;
    private String loginTime;
    private String ipAddress;
    private String deviceInfo;
    private String supportPhone;
    private String supportEmail;

    public LoginAlertNotification() {}

    @JsonCreator
    public LoginAlertNotification(
            @JsonProperty("email")        String email,
            @JsonProperty("fullName")     String fullName,
            @JsonProperty("username")     String username,
            @JsonProperty("loginTime")    String loginTime,
            @JsonProperty("ipAddress")    String ipAddress,
            @JsonProperty("deviceInfo")   String deviceInfo,
            @JsonProperty("supportPhone") String supportPhone,
            @JsonProperty("supportEmail") String supportEmail) {
        this.email        = email;
        this.fullName     = fullName;
        this.username     = username;
        this.loginTime    = loginTime;
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

    public String getLoginTime()       { return loginTime; }
    public void setLoginTime(String v) { this.loginTime = v; }

    public String getIpAddress()       { return ipAddress; }
    public void setIpAddress(String v) { this.ipAddress = v; }

    public String getDeviceInfo()       { return deviceInfo; }
    public void setDeviceInfo(String v) { this.deviceInfo = v; }

    public String getSupportPhone()       { return supportPhone; }
    public void setSupportPhone(String v) { this.supportPhone = v; }

    public String getSupportEmail()       { return supportEmail; }
    public void setSupportEmail(String v) { this.supportEmail = v; }
}
