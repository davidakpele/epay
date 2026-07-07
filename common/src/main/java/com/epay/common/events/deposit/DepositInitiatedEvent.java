package com.epay.common.events.deposit;

import com.epay.common.events.DomainEvent;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Published when a deposit is initiated and a payment URL is returned to the user.
 * History module listens to record the pending transaction.
 */
@Getter
public class DepositInitiatedEvent extends DomainEvent {

    private final Long       userId;
    private final String     reference;
    private final String     idempotencyKey;
    private final BigDecimal amount;
    private final String     currency;
    private final String     channel;
    private final String     paymentUrl;

    public DepositInitiatedEvent(Long userId, String reference, String idempotencyKey,
                                  BigDecimal amount, String currency,
                                  String channel, String paymentUrl) {
        super();
        this.userId         = userId;
        this.reference      = reference;
        this.idempotencyKey = idempotencyKey;
        this.amount         = amount;
        this.currency       = currency;
        this.channel        = channel;
        this.paymentUrl     = paymentUrl;
    }
}
