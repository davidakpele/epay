package pesco.deposit_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletSectionDTO {
    private Long walletId;
    private boolean hasTransferPin;
    private List<WalletBalanceDTO> walletBalances;
}
