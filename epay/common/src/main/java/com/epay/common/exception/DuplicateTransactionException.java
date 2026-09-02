package com.epay.common.exception;

import org.springframework.http.HttpStatus;

public class DuplicateTransactionException extends BaseException {
    
    public DuplicateTransactionException(String message) {
        super(message, ErrorCode.DUPLICATE_TRANSACTION, HttpStatus.CONFLICT);
    }
    
    public static DuplicateTransactionException withReference(String reference) {
        return new DuplicateTransactionException("A transaction with this reference has already been processed");
    }
}
