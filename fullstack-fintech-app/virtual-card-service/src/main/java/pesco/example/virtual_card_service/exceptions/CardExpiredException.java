package pesco.example.virtual_card_service.exceptions;

public class CardExpiredException extends VirtualCardException {
    public CardExpiredException(String cardId) {
        super("Card has expired: " + cardId);
    }
}
