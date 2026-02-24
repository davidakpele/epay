package pesco.example.virtual_card_service.exceptions;

public class SpendingLimitExceededException extends VirtualCardException {
    public SpendingLimitExceededException(String cardId) {
        super("Spending limit exceeded for card: " + cardId);
    }
}
