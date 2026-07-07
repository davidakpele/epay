package com.epay.common.interfaces;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Port for recording deposit events in the history module.
 * Implemented by DepositHistoryAdapter in epay-history.
 */
public interface IDepositHistoryPort {

    void recordDepositInitiated(Long userId, String reference, BigDecimal amount,
                                String currency, String channel);

    void recordDepositCompleted(Long userId, String reference, BigDecimal amount,
                                String currency, String gatewayReference, LocalDateTime completedAt);

    void recordDepositFailed(Long userId, String reference, BigDecimal amount,
                             String currency, String reason);
}
