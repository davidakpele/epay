package com.epay.common.exception;

public class DuplicateCardException extends VirtualCardException {

    public DuplicateCardException(String message) {
        super(message, ErrorCode.RESOURCE_ALREADY_EXISTS);
    }
}