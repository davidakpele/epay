package pesco.notification_service.payloads;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordResetRequest {
    @NotBlank(message = "Email is mandatory")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Username is mandatory")
    private String username;

    @NotBlank(message = "Message is mandatory")
    private String message;

    @NotBlank(message = "URL is mandatory")
    private String url;

    // Default constructor
    public PasswordResetRequest() {
    }

    // Constructor to map from JSON
    @JsonCreator
    public PasswordResetRequest(
            @JsonProperty("email") String email,
            @JsonProperty("username") String username,
            @JsonProperty("message") String message,
            @JsonProperty("url") String url) {
        this.email = email;
        this.username = username;
        this.message = message;
        this.url = url;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getmessage() {
        return message;
    }

    public void setmessage(String message) {
        this.message = message;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
