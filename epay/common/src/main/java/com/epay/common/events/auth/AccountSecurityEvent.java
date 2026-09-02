package com.epay.common.events.auth;

import com.epay.common.events.DomainEvent;
import lombok.Getter;

@Getter
public class AccountSecurityEvent extends DomainEvent {

    public enum SecurityAction {
        PASSWORD_CHANGED,
        TWO_FACTOR_ENABLED,
        TWO_FACTOR_DISABLED,
        ACCOUNT_LOCKED,
        ACCOUNT_UNLOCKED,
        SUSPICIOUS_LOGIN_DETECTED,
        ACCOUNT_DELETION_REQUESTED
    }

    private final Long          userId;
    private final String        username;
    private final String        email;
    private final SecurityAction action;
    private final String        ipAddress;
    private final String        device;

    public AccountSecurityEvent(Long userId, String username, String email,
                                SecurityAction action, String ipAddress, String device) {
        super();
        this.userId    = userId;
        this.username  = username;
        this.email     = email;
        this.action    = action;
        this.ipAddress = ipAddress;
        this.device    = device;
    }
}
