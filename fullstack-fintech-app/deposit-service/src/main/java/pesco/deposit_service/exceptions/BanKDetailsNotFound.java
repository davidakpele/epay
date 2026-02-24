package pesco.deposit_service.exceptions;

public class BanKDetailsNotFound extends RuntimeException{
    private final String details;

    public BanKDetailsNotFound(String message, String details) {
        super(message);
        this.details = details;
    }

    public String getDetails() {
        return details;
    }
}