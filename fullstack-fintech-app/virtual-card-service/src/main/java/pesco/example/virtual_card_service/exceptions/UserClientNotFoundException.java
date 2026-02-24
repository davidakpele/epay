package pesco.example.virtual_card_service.exceptions;

public class UserClientNotFoundException extends RuntimeException {
    private String details;

    public UserClientNotFoundException(String message, String details) {
        super(message);
        this.details = details;
    }

    public String getDetails() {
        return details;
    }
}