package pesco.notification_service.payloads;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Generic security-event payload used for:
 *  - password reset confirmation
 *  - update password
 *  - deactivate account
 *  - account lock / unlock
 *  - account block / unblock
 *  - 2FA enabled / disabled
 */
@Data
public class AccountSecurityNotification {

    /** The recipient's email address */
    private String email;
    /** Full display name, e.g. "David Akpele" */
    private String fullName;
    /** Username / login handle */
    private String username;
    /**
     * Event discriminator. Expected values:
     * PASSWORD_RESET | UPDATE_PASSWORD | DEACTIVATE_ACCOUNT |
     * ACCOUNT_LOCKED | ACCOUNT_UNLOCKED | ACCOUNT_BLOCKED | ACCOUNT_UNBLOCKED |
     * TWO_FACTOR_ENABLED | TWO_FACTOR_DISABLED
     */
    private String eventType;
    /** ISO-8601 / formatted timestamp when the event occurred */
    private String eventTime;
    /** IP address from which the action was performed */
    private String ipAddress;
    /** Device / browser info */
    private String deviceInfo;
    /** Support phone number */
    private String supportPhone;
    /** Support email address */
    private String supportEmail;

    public AccountSecurityNotification() {}

    @JsonCreator
    public AccountSecurityNotification(
            @JsonProperty("email")        String email,
            @JsonProperty("fullName")     String fullName,
            @JsonProperty("username")     String username,
            @JsonProperty("eventType")    String eventType,
            @JsonProperty("eventTime")    String eventTime,
            @JsonProperty("ipAddress")    String ipAddress,
            @JsonProperty("deviceInfo")   String deviceInfo,
            @JsonProperty("supportPhone") String supportPhone,
            @JsonProperty("supportEmail") String supportEmail) {
        this.email        = email;
        this.fullName     = fullName;
        this.username     = username;
        this.eventType    = eventType;
        this.eventTime    = eventTime;
        this.ipAddress    = ipAddress;
        this.deviceInfo   = deviceInfo;
        this.supportPhone = supportPhone;
        this.supportEmail = supportEmail;
    }

    public String getEmail()        { return email; }
    public void setEmail(String v)  { this.email = v; }

    public String getFullName()        { return fullName; }
    public void setFullName(String v)  { this.fullName = v; }

    public String getUsername()        { return username; }
    public void setUsername(String v)  { this.username = v; }

    public String getEventType()        { return eventType; }
    public void setEventType(String v)  { this.eventType = v; }

    public String getEventTime()        { return eventTime; }
    public void setEventTime(String v)  { this.eventTime = v; }

    public String getIpAddress()        { return ipAddress; }
    public void setIpAddress(String v)  { this.ipAddress = v; }

    public String getDeviceInfo()        { return deviceInfo; }
    public void setDeviceInfo(String v)  { this.deviceInfo = v; }

    public String getSupportPhone()        { return supportPhone; }
    public void setSupportPhone(String v)  { this.supportPhone = v; }

    public String getSupportEmail()        { return supportEmail; }
    public void setSupportEmail(String v)  { this.supportEmail = v; }
}
