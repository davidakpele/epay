package com.epay.deposit.gateway;

import com.epay.domain.deposit.enums.DepositChannel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DepositGatewayFactory {

    private final PaystackDepositGateway paystackGateway;

    public DepositGateway getGateway(DepositChannel channel) {
        return switch (channel) {
            case PAYSTACK -> paystackGateway;
            case FLUTTERWAVE -> throw new UnsupportedOperationException("Flutterwave not yet implemented");
            default -> throw new IllegalArgumentException("Unsupported deposit channel: " + channel);
        };
    }
}
