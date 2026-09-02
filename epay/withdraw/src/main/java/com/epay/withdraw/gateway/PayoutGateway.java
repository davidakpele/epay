package com.epay.withdraw.gateway;

import java.math.BigDecimal;

public interface PayoutGateway {
    PayoutResult payout(String reference, String accountNumber, String bankCode,
                        String accountName, BigDecimal amount, String currency, String narration);
}
