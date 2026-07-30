package com.epay.common.exception;

public class CardNotFoundException extends VirtualCardException {

    public CardNotFoundException(String cardId) {
        super("Virtual card not found with ID: " + cardId, ErrorCode.CARD_NOT_FOUND);
    }
}
