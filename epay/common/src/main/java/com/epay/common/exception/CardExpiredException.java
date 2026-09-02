package com.epay.common.exception;

public class CardExpiredException extends VirtualCardException {

    public CardExpiredException(String cardId) {
        super("Card has expired: " + cardId, ErrorCode.OPERATION_NOT_ALLOWED);
    }
}