package pesco.notification_service.payloads;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserOTPMessage {
    @NotBlank(message = "Email is mandatory")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "OTP is mandatory")
    @Pattern(regexp = "\\d{6}", message = "OTP must be a 6-digit number")
    private String otp;
    
    @NotBlank(message = "Reset password field is mandatory")
    private String restPassword;

    @NotBlank(message = "Two-factor authentication configuration is mandatory")
    private String configTwoFactorAuth;

    @NotBlank(message = "Two-factor authentication recovery configuration is mandatory")
    private String configTwoFactorAuthRecovery;

    // Default constructor
    public UserOTPMessage() {
    }

    // Constructor to map from JSON
    @JsonCreator
    public UserOTPMessage(
            @JsonProperty("email") String email,
            @JsonProperty("otp") String otp,
            @JsonProperty("restPassword") String restPassword,
            @JsonProperty("configTwoFactorAuth") String configTwoFactorAuth,
            @JsonProperty("configTwoFactorAuthRecovery") String configTwoFactorAuthRecovery) {
        this.email = email;
        this.otp = otp;
        this.restPassword = restPassword;
        this.configTwoFactorAuth = configTwoFactorAuth;
        this.configTwoFactorAuthRecovery = configTwoFactorAuthRecovery;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getRestPassword() {
        return restPassword;
    }

    public void setRestPassword(String restPassword) {
        this.restPassword = restPassword;
    }

    public String getConfigTwoFactorAuth() {
        return configTwoFactorAuth;
    }

    public void setConfigTwoFactorAuth(String configTwoFactorAuth) {
        this.configTwoFactorAuth = configTwoFactorAuth;
    }

    public String getConfigTwoFactorAuthRecovery() {
        return configTwoFactorAuthRecovery;
    }

    public void setConfigTwoFactorAuthRecovery(String configTwoFactorAuthRecovery) {
        this.configTwoFactorAuthRecovery = configTwoFactorAuthRecovery;
    }
}
