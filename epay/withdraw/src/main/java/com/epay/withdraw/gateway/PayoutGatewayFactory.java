package com.epay.withdraw.gateway;

import com.epay.domain.withdraw.enums.WithdrawalType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PayoutGatewayFactory {

    private final PaystackPayoutGateway paystackGateway;

    public PayoutGateway getGateway(WithdrawalType type) {
        return switch (type) {
            case BANK_TRANSFER, USSD, CARD -> paystackGateway;
            default -> throw new UnsupportedOperationException("No gateway for: " + type);
        };
    }
}
