package pesco.deposit_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletBalanceDTO {
    private String currency_code;
    private String symbol;
    private String balance;
}
