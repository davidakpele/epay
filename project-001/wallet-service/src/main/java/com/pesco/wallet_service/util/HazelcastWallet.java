package com.pesco.wallet_service.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.pesco.wallet_service.bootstrap.UserSessionData;
import com.pesco.wallet_service.dtos.WalletBalanceDTO;
import com.pesco.wallet_service.dtos.WalletSection;
import com.pesco.wallet_service.enums.Currency;

@Component
public class HazelcastWallet {

    @Autowired
    private HazelcastInstance hazelcastInstance;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public void updateHazelcastWalletBalance(Long userId, Currency currencyType, BigDecimal amountDelta)
            throws JsonProcessingException {
        String mapKey = "user_session:" + userId;
        IMap<String, String> userSessionMap = hazelcastInstance.getMap("user-sessions");
        
        String sessionData = userSessionMap.get(mapKey);
        if (sessionData != null) {
            UserSessionData userSession = objectMapper.readValue(sessionData, UserSessionData.class);
            WalletSection walletSection = userSession.getWallet();
            List<WalletBalanceDTO> balances = walletSection.getWallet_balances();

            boolean found = false;
            for (WalletBalanceDTO dto : balances) {
                if (dto.getCurrency_code().equalsIgnoreCase(currencyType.name())) {
                    Object balanceObj = dto.getBalance();
                    BigDecimal current;

                    if (balanceObj instanceof String) {
                        current = new BigDecimal(((String) balanceObj).replace(",", ""));
                    } else if (balanceObj instanceof Number) {
                        current = BigDecimal.valueOf(((Number) balanceObj).doubleValue());
                    } else {
                        throw new IllegalArgumentException("Unsupported balance type: " + balanceObj.getClass());
                    }
                    BigDecimal updated = current.add(amountDelta);
                    dto.setBalance(updated.setScale(2, RoundingMode.HALF_UP).toPlainString());
                    found = true;
                    break;
                }
            }

            if (!found && amountDelta.compareTo(BigDecimal.ZERO) > 0) {
                WalletBalanceDTO newDto = new WalletBalanceDTO();
                newDto.setCurrency_code(currencyType.name());
                newDto.setBalance(amountDelta.setScale(2, RoundingMode.HALF_UP).toPlainString());
                balances.add(newDto);
            }
            walletSection.setWallet_balances(balances);
            userSession.setWallet(walletSection);

            String updatedJson = objectMapper.writeValueAsString(userSession);
            userSessionMap.put(mapKey, updatedJson);
        }
    }
}