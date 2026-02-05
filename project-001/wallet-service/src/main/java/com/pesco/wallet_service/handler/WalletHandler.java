package com.pesco.wallet_service.handler;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import jakarta.transaction.Transactional;
import com.pesco.wallet_service.dtos.UserDTO;
import com.pesco.wallet_service.dtos.WalletBalanceDTO;
import com.pesco.wallet_service.dtos.WalletSection;
import com.pesco.wallet_service.models.Wallet;
import com.pesco.wallet_service.models.WalletSettings;
import com.pesco.wallet_service.repository.WalletRepository;
import com.pesco.wallet_service.repository.WalletSettingsRepository;
import com.pesco.wallet_service.util.AccountWrapper;

@Component
public class WalletHandler {

    private final WalletRepository walletRepository;
    private final AccountWrapper accountWrapper;
    private final WalletSettingsRepository walletSettingsRepository;

    public WalletHandler(WalletRepository walletRepository, AccountWrapper accountWrapper, WalletSettingsRepository walletSettingsRepository) {
        this.walletRepository = walletRepository;
        this.accountWrapper = accountWrapper;
        this.walletSettingsRepository = walletSettingsRepository;
    }

    @Transactional
    public WalletSection buildWalletSection(UserDTO user) {
        Optional<Wallet> walletOptional = walletRepository.findByUserId(user.getId());
          
        WalletSection walletSection = new WalletSection();
        if (walletOptional.isEmpty()) {
            walletSection.setWallet_balances(new ArrayList<>());
            walletSection.setWalletId(null);
            walletSection.setHasTransferPin(false);
            return walletSection;
        }

        Wallet wallet = walletOptional.get();
        Optional<WalletSettings> walletSetting = walletSettingsRepository.findByWalletId(wallet.getId());  

        // Convert balances to WalletBalanceDTO using setters
        List<WalletBalanceDTO> balances = wallet.getBalances().stream().map(balance -> {
            WalletBalanceDTO dto = new WalletBalanceDTO();
            dto.setCurrency_code(balance.getCurrencyCode());
            dto.setSymbol(balance.getCurrencySymbol());
            dto.setBalance(FormatBigDecimal(balance.getBalance()));
            return dto;
        }).collect(Collectors.toList());

        boolean hasPin = walletSetting.get().getIsSecure();

        walletSection.setWallet_balances(balances);
        walletSection.setWalletId(wallet.getId());
        walletSection.setHasTransferPin(hasPin);

        return walletSection;
    }

    public String FormatBigDecimal(BigDecimal value) {
        String pattern = "#,##0.00";
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat(pattern, symbols);
        return decimalFormat.format(value);
    }
    
    public void createAccount(Long userId) {
        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        wallet.setBalances(new ArrayList<>());
        accountWrapper.initializeAllCurrencyWallets(wallet);

        walletRepository.save(wallet);

        WalletSettings setting = new WalletSettings();
        setting.setPassword("");
        setting.setIsSecure(false);
        setting.setWallet(wallet);
        walletSettingsRepository.save(setting);
    }

}
