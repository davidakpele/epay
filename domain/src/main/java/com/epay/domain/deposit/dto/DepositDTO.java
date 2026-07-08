package com.epay.domain.deposit.dto;

import com.epay.domain.deposit.enums.DepositChannel;
import com.epay.domain.deposit.enums.DepositStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DepositDTO {

    private Long        id;
    private String      reference;
    private BigDecimal  amount;
    private String      currency;
    private DepositChannel channel;
    private DepositStatus  status;
    private String      paymentUrl;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
