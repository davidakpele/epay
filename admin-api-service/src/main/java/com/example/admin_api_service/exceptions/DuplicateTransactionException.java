package com.example.admin_api_service.exceptions;

public class DuplicateTransactionException extends RuntimeException {
    public DuplicateTransactionException(String reference) {
        super("Transaction already exists with reference: " + reference);
    }
}
