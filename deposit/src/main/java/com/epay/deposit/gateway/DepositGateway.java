package com.epay.deposit.gateway;

import java.math.BigDecimal;

public interface DepositGateway {

    String initiate(String reference, String email, BigDecimal amount, String currency, String callbackUrl);
    DepositVerificationResult verify(String reference);
    boolean validateWebhookSignature(String payload, String signature);
}
