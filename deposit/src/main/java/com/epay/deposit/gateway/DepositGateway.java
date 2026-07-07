package com.epay.deposit.gateway;

import java.math.BigDecimal;

/**
 * Strategy interface for payment gateways.
 * Each gateway (Paystack, Flutterwave) implements this.
 */
public interface DepositGateway {

    /**
     * Initiates a deposit and returns the payment URL for the user.
     *
     * @param reference    platform-generated unique reference
     * @param email        user's email (required by most gateways)
     * @param amount       deposit amount
     * @param currency     ISO 4217 currency code
     * @param callbackUrl  URL to redirect after payment
     * @return payment URL to redirect the user to
     */
    String initiate(String reference, String email, BigDecimal amount,
                    String currency, String callbackUrl);

    /**
     * Verifies a deposit with the gateway after receiving a webhook or user redirect.
     *
     * @param reference  the gateway reference or platform reference
     * @return verified deposit amount
     */
    DepositVerificationResult verify(String reference);

    /**
     * Validates the webhook signature to confirm the request came from the gateway.
     *
     * @param payload   raw request body
     * @param signature value from the gateway's signature header
     * @return true if valid
     */
    boolean validateWebhookSignature(String payload, String signature);
}
