package com.epay.domain.admin.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminWalletDTO {
    private Long walletId;
    private Long userId;
    private boolean active;
    private boolean pinSet;
    private List<BalanceEntry> balances;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BalanceEntry {
        private String currency;
        private String symbol;
        private BigDecimal balance;
        private boolean isDefault;
    }
}
