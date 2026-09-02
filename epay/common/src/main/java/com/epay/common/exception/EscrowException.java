package com.epay.common.exception;

import org.springframework.http.HttpStatus;

public class EscrowException extends BaseException {
    
    public EscrowException(String message, String errorCode) {
        super(message, errorCode, HttpStatus.BAD_REQUEST);
    }
    
    public static EscrowException notFound() {
        return new EscrowException("Escrow not found", ErrorCode.ESCROW_NOT_FOUND);
    }
    
    public static EscrowException alreadyReleased() {
        return new EscrowException("Escrow has already been released", ErrorCode.ESCROW_ALREADY_RELEASED);
    }
    
    public static EscrowException alreadyCancelled() {
        return new EscrowException("Escrow has already been cancelled", ErrorCode.ESCROW_ALREADY_CANCELLED);
    }
    
    public static EscrowException expired() {
        return new EscrowException("Escrow has expired", ErrorCode.ESCROW_EXPIRED);
    }
    
    public static EscrowException underDispute() {
        return new EscrowException("Escrow is under dispute and cannot be modified", ErrorCode.ESCROW_DISPUTED);
    }
    
    public static EscrowException unauthorized() {
        return new EscrowException("You are not authorized to perform this action on the escrow", 
            ErrorCode.ESCROW_UNAUTHORIZED);
    }
}
