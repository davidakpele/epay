package com.epay.common.exception;

import org.springframework.http.HttpStatus;

public class BlacklistedException extends BaseException {
    
    public BlacklistedException(String message, String errorCode) {
        super(message, errorCode, HttpStatus.FORBIDDEN);
    }
    
    public static BlacklistedException accountBlacklisted(String accountId) {
        return new BlacklistedException(
            "This account has been blacklisted", 
            ErrorCode.ACCOUNT_BLACKLISTED
        );
    }
    
    public static BlacklistedException ipBlacklisted(String ip) {
        return new BlacklistedException(
            "Access from this IP address is not allowed", 
            ErrorCode.IP_BLACKLISTED
        );
    }
    
    public static BlacklistedException deviceBlacklisted(String deviceId) {
        return new BlacklistedException(
            "This device has been blacklisted", 
            ErrorCode.DEVICE_BLACKLISTED
        );
    }
}
