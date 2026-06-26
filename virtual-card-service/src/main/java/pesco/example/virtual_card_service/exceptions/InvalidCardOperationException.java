package pesco.example.virtual_card_service.exceptions;

public class InvalidCardOperationException extends VirtualCardException {
    public InvalidCardOperationException(String message) {
        super(message);
    }
}
