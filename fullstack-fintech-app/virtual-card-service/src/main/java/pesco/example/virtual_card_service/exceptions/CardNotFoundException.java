package pesco.example.virtual_card_service.exceptions;

public class CardNotFoundException extends VirtualCardException {
    public CardNotFoundException(String cardId) {
        super("Virtual card not found with ID: " + cardId);
    }
}
