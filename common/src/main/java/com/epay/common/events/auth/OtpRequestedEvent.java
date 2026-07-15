package com.epay.common.events.auth;

import com.epay.common.events.DomainEvent;
import com.epay.domain.auth.enums.ContactMethod;
import com.epay.domain.auth.enums.TokenPurpose;
import lombok.Getter;

@Getter
public class OtpRequestedEvent extends DomainEvent {

    private final Long          userId;
    private final String        email;
    private final String        phoneNumber;
    private final String        username;
    private final String        rawOtp;       
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
