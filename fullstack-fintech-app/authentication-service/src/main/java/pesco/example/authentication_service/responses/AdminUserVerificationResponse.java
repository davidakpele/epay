package pesco.example.authentication_service.responses;

public class AdminUserVerificationResponse {

    private boolean verified;
    private String username;

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
