package pesco.notification_service.payloads;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Payload for the forgot-password OTP email.
 * Carries the 4-digit OTP (not a link) that the user enters on the reset form.
 */
@Data
public class ForgotPasswordOtpPayload {

    private String email;
    private String username;
    /** The 4-digit OTP to embed in the email */
    private String otp;

    public ForgotPasswordOtpPayload() {}

    @JsonCreator
    public ForgotPasswordOtpPayload(
            @JsonProperty("email")    String email,
            @JsonProperty("username") String username,
            @JsonProperty("otp")      String otp) {
        this.email    = email;
        this.username = username;
        this.otp      = otp;
    }

    public String getEmail()    { return email; }
    public void setEmail(String v)    { this.email = v; }

    public String getUsername() { return username; }
    public void setUsername(String v) { this.username = v; }

    public String getOtp()      { return otp; }
    public void setOtp(String v)      { this.otp = v; }
}
