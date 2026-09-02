package com.epay.withdraw.gateway;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PayoutResult {
    private boolean success;
    private String  gatewayReference;
    private String  failureReason;
}
