package com.epay.common.exception;

public class InvalidCardOperationException extends VirtualCardException {

    public InvalidCardOperationException(String message) {
        super(message, ErrorCode.OPERATION_NOT_ALLOWED);
    }
}