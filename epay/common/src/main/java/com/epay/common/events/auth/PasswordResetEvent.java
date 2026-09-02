package com.epay.common.events.auth;

import com.epay.common.events.DomainEvent;
import lombok.Getter;

@Getter
public class PasswordResetEvent extends DomainEvent {

    private final Long   userId;
    private final String username;
    private final String email;
    private final String ipAddress;
    private final String device;

    public PasswordResetEvent(Long userId, String username, String email,
                              String ipAddress, String device) {
        super();
        this.userId    = userId;
        this.username  = username;
        this.email     = email;
        this.ipAddress = ipAddress;
        this.device    = device;
    }
}
