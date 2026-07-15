package com.epay.common.events.auth;

import com.epay.common.events.DomainEvent;
import com.epay.domain.auth.enums.AccountType;
import com.epay.domain.auth.enums.ContactMethod;
import lombok.Getter;

@Getter
public class UserRegisteredEvent extends DomainEvent {

    private final Long   userId;
    private final String username;
    private final String email;
    private final String phoneNumber;
    private final String firstName;
    private final String lastName;
    private final AccountType  accountType;
    private final ContactMethod registrationChannel;

    public UserRegisteredEvent(Long userId, String username, String email,
                               String phoneNumber, String firstName, String lastName,
                               AccountType accountType, ContactMethod registrationChannel) {
        super();
        this.userId              = userId;
        this.username            = username;
        this.email               = email;
        this.phoneNumber         = phoneNumber;
        this.firstName           = firstName;
        this.lastName            = lastName;
        this.accountType         = accountType;
        this.registrationChannel = registrationChannel;
    }
}
