package com.epay.common.exception;

import org.springframework.http.HttpStatus;

public class VirtualCardException extends BaseException {
    
    public VirtualCardException(String message, String errorCode) {
        super(message, errorCode, HttpStatus.BAD_REQUEST);
    }
    
    public VirtualCardException(String message, String errorCode, HttpStatus status) {
        super(message, errorCode, status);
    }
    
    public static VirtualCardException notFound() {
        return new VirtualCardException("Virtual card not found", ErrorCode.CARD_NOT_FOUND);
    }
    
    public static VirtualCardException isFrozen() {
        return new VirtualCardException("Card is frozen and cannot be used", ErrorCode.CARD_FROZEN);
    }
    
    public static VirtualCardException isTerminated() {
        return new VirtualCardException("Card has been terminated", ErrorCode.CARD_TERMINATED);
    }
    
    public static VirtualCardException maxLimitReached() {
        return new VirtualCardException("Maximum number of virtual cards reached", ErrorCode.MAX_CARD_LIMIT_REACHED);
    }
    
    public static VirtualCardException fundingFailed() {
        return new VirtualCardException("Card funding failed", ErrorCode.CARD_FUNDING_FAILED);
    }
    
    public static VirtualCardException providerError() {
        return new VirtualCardException("Card provider error. Please try again", 
            ErrorCode.CARD_PROVIDER_ERROR, HttpStatus.BAD_GATEWAY);
    }
}
