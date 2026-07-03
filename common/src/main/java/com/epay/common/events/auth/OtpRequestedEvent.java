package com.epay.common.events.auth;

import com.epay.common.events.DomainEvent;
import com.epay.domain.auth.enums.ContactMethod;
import com.epay.domain.auth.enums.TokenPurpose;
import lombok.Getter;

/**
 * Published when an OTP needs to be delivered to the user.
 * Consumed by:
 *  - NotificationModule → sends OTP via the specified channel
 *
 * The raw OTP is included here only for delivery — it is never persisted
 * by any listener. Only the hash is stored in the database.
 */
@Getter
public class OtpRequestedEvent extends DomainEvent {

    private final Long          userId;
    private final String        email;
    private final String        phoneNumber;
    private final String        username;
    private final String        rawOtp;       // used for delivery only — never stored
    private final TokenPurpose  purpose;
    private final ContactMethod channel;

    public OtpRequestedEvent(Long userId, String email, String phoneNumber,
                             String username, String rawOtp,
                             TokenPurpose purpose, ContactMethod channel) {
        super();
        this.userId      = userId;
        this.email       = email;
        this.phoneNumber = phoneNumber;
        this.username    = username;
        this.rawOtp      = rawOtp;
        this.purpose     = purpose;
        this.channel     = channel;
    }
}
