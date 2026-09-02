package com.epay.common.events.deposit;

import com.epay.common.events.DomainEvent;
import lombok.Getter;

import java.math.BigDecimal;


@Getter
public class DepositCompletedEvent extends DomainEvent {

    private final Long       userId;
    private final String     reference;
    private final String     gatewayReference;
    private final BigDecimal amount;
    private final String     currency;
    private final String     channel;
    private final BigDecimal newBalance;

    public DepositCompletedEvent(Long userId, String reference, String gatewayReference,
                                  BigDecimal amount, String currency,
                                  String channel, BigDecimal newBalance) {
        super();
        this.userId           = userId;
        this.reference        = reference;
        this.gatewayReference = gatewayReference;
        this.amount           = amount;
        this.currency         = currency;
        this.channel          = channel;
        this.newBalance       = newBalance;
    }
}
