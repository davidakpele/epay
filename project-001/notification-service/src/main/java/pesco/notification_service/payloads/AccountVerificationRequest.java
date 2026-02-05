package pesco.notification_service.payloads;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AccountVerificationRequest {
    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Message is mandatory")
    private String message;

    @NotBlank(message = "Link is mandatory")
    private String link;

    @NotBlank(message = "Username is mandatory")
    private String username;

    // Default constructor
    public AccountVerificationRequest() {
    }

    // Constructor to map from JSON
    @JsonCreator
    public AccountVerificationRequest(
            @JsonProperty("email") String email,
            @JsonProperty("message") String message,
            @JsonProperty("link") String link,
            @JsonProperty("username") String username) {
        this.email = email;
        this.message = message;
        this.link = link;
        this.username = username;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getmessage() {
        return message;
    }

    public void setmessage(String message) {
        this.message = message;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
