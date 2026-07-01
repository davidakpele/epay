package pesco.notification_service.payloads;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Payload for the forgot-username email.
 * Sends the user's username back to their registered email.
 */
@Data
public class ForgotUsernamePayload {

    private String email;
    private String username;
    private String fullName;

    public ForgotUsernamePayload() {}

    @JsonCreator
    public ForgotUsernamePayload(
            @JsonProperty("email")    String email,
            @JsonProperty("username") String username,
            @JsonProperty("fullName") String fullName) {
        this.email    = email;
        this.username = username;
        this.fullName = fullName;
    }

    public String getEmail()    { return email; }
    public void setEmail(String v)    { this.email = v; }

    public String getUsername() { return username; }
    public void setUsername(String v) { this.username = v; }

    public String getFullName() { return fullName; }
    public void setFullName(String v) { this.fullName = v; }
}
