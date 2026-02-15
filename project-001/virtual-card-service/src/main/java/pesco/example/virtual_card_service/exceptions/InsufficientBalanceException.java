package pesco.example.virtual_card_service.exceptions;

public class InsufficientBalanceException extends VirtualCardException {
    public InsufficientBalanceException(String cardId) {
        super("Insufficient balance for card: " + cardId);
    }
}
