package com.epay.domain.bills.dto;

import com.epay.domain.bills.enums.BillService;
import com.epay.domain.bills.enums.BillStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BillPaymentResponse {

    private String status;

    private String message;

    private BillData data;

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BillData {
        private String transactionId;
        private String referenceId;
        private BillService service;
        private String provider;

        private BigDecimal amount;

        private String currency;
        private String recipient;
        private BillStatus status;
        private Instant processedAt;
    }
}
