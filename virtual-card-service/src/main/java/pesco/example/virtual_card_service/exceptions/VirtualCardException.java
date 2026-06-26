package pesco.example.virtual_card_service.exceptions;

public class VirtualCardException extends RuntimeException {
    public VirtualCardException(String message) {
        super(message);
    }
    
    public VirtualCardException(String message, Throwable cause) {
        super(message, cause);
    }
}