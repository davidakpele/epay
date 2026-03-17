package com.example.admin_api_service.exceptions;

public class InsufficientLiquidityException extends RuntimeException {
    public InsufficientLiquidityException(String message) { super(message); }
}
