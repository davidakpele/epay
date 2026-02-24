package pesco.example.virtual_card_service.exceptions;

public class CardNotActiveException extends VirtualCardException {
    public CardNotActiveException(String cardId) {
        super("Card is not active: " + cardId);
    }
}