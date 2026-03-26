package com.example.admin_api_service.payloads;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarkExecutedRequest {
    @NotNull(message = "Balance after is required")
    private BigDecimal balanceAfter;
 
    private String historyReferenceId;
 
    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(BigDecimal balanceAfter) { this.balanceAfter = balanceAfter; }
    public String getHistoryReferenceId() { return historyReferenceId; }
    public void setHistoryReferenceId(String historyReferenceId) { this.historyReferenceId = historyReferenceId; }
}
