package com.epay.common.events.auth;

import com.epay.common.events.DomainEvent;
import lombok.Getter;

/**
 * Published on every successful login.
 * Consumed by:
 *  - NotificationModule → sends login alert email (new IP or new device)
 *  - HistoryModule      → writes login audit entry
 */
@Getter
public class UserLoginEvent extends DomainEvent {

    private final Long    userId;
    private final String  username;
    private final String  email;
    private final String  ipAddress;
    private final String  device;
    private final boolean newDevice;

    public UserLoginEvent(Long userId, String username, String email,
                          String ipAddress, String device, boolean newDevice) {
        super();
        this.userId    = userId;
        this.username  = username;
        this.email     = email;
        this.ipAddress = ipAddress;
        this.device    = device;
        this.newDevice = newDevice;
    }
}
