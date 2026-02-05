package pesco.example.authentication_service.payloads;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserSignUpRequest {
    
    private String name;
    
    private String email;

    private String phone;

    @NotBlank(message = "Registration mode is required")
    private String regMode; // "email" or "phone"

    private String verificationMethod; // "EMAIL", "SMS", or "WHATSAPP"

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 50, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

    @NotBlank(message = "Verification code is required")
    private String verificationCode;

    public UserSignUpRequest() {
    }

    public UserSignUpRequest(String name, String email, String phone, String regMode, 
                           String verificationMethod, String username, String password, 
                           String confirmPassword, String verificationCode) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.regMode = regMode;
        this.verificationMethod = verificationMethod;
        this.username = username;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.verificationCode = verificationCode;
    }

    // Getters and setters
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return this.phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRegMode() {
        return this.regMode;
    }

    public void setRegMode(String regMode) {
        this.regMode = regMode;
    }

    public String getVerificationMethod() {
        return this.verificationMethod;
    }

    public void setVerificationMethod(String verificationMethod) {
        this.verificationMethod = verificationMethod;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return this.confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getVerificationCode() {
        return this.verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }
}