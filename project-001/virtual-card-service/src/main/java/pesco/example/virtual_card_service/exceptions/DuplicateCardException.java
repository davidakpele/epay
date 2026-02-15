package pesco.example.virtual_card_service.exceptions;

public class DuplicateCardException extends VirtualCardException {
    public DuplicateCardException(String message) {
        super(message);
    }
}