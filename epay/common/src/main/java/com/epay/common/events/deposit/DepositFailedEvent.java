package com.epay.common.events.deposit;

import com.epay.common.events.DomainEvent;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class DepositFailedEvent extends DomainEvent {

    private final Long       userId;
    private final String     reference;
    private final BigDecimal amount;
    private final String     currency;
    private final String     failureReason;

    public DepositFailedEvent(Long userId, String reference,
                               BigDecimal amount, String currency, String failureReason) {
        super();
        this.userId        = userId;
        this.reference     = reference;
        this.amount        = amount;
        this.currency      = currency;
        this.failureReason = failureReason;
    }
}
