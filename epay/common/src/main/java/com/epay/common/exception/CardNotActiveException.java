package com.epay.common.exception;

public class CardNotActiveException extends VirtualCardException {

    public CardNotActiveException(String cardId) {
        super("Card is not active: " + cardId, ErrorCode.OPERATION_NOT_ALLOWED);
    }
}