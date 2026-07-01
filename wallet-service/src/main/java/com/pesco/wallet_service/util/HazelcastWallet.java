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

/**
 * Keeps the Hazelcast wallet-session cache in sync after every balance mutation.
 *
 * <p>The {@code currencyCode} parameter is now a plain {@code String} (e.g. "NGN")
 * instead of the old {@code Currency} enum, so newly-added currencies work without
 * code changes.
 */
@Component
public class HazelcastWallet {

    @Autowired
    private HazelcastInstance hazelcastInstance;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Applies {@code amountDelta} (positive = credit, negative = debit) to the
     * cached wallet balance for the given user and currency code.
     *
     * @param userId       the wallet owner
     * @param currencyCode ISO 4217 code, e.g. "USD" (case-insensitive)
     * @param amountDelta  the signed amount to add to the cached balance
     */
    public void updateHazelcastWalletBalance(Long userId, String currencyCode, BigDecimal amountDelta) {
        String mapKey = "user_session:" + userId;
        IMap<String, String> userSessionMap = hazelcastInstance.getMap("user-sessions");

        String sessionData = userSessionMap.get(mapKey);
        if (sessionData == null) return;

        try {
            UserSessionData userSession = objectMapper.readValue(sessionData, UserSessionData.class);
            WalletSection walletSection = userSession.getWallet();
            List<WalletBalanceDTO> balances = walletSection.getWallet_balances();

            boolean found = false;
            for (WalletBalanceDTO dto : balances) {
                if (dto.getCurrency_code().equalsIgnoreCase(currencyCode)) {
                    Object balanceObj = dto.getBalance();
                    BigDecimal current;
                    if (balanceObj instanceof String) {
                        current = new BigDecimal(((String) balanceObj).replace(",", ""));
                    } else if (balanceObj instanceof Number) {
                        current = BigDecimal.valueOf(((Number) balanceObj).doubleValue());
                    } else {
                        throw new IllegalArgumentException("Unsupported balance type: " + balanceObj.getClass());
                    }

                    dto.setBalance(current.add(amountDelta)
                            .setScale(2, RoundingMode.HALF_UP)
                            .toPlainString());
                    found = true;
                    break;
                }
            }

            // If the currency is new (just added by admin) and has a positive delta, add it
            if (!found && amountDelta.compareTo(BigDecimal.ZERO) > 0) {
                WalletBalanceDTO newDto = new WalletBalanceDTO();
                newDto.setCurrency_code(currencyCode.toUpperCase());
                newDto.setBalance(amountDelta.setScale(2, RoundingMode.HALF_UP).toPlainString());
                balances.add(newDto);
            }

            walletSection.setWallet_balances(balances);
            userSession.setWallet(walletSection);
            userSessionMap.put(mapKey, objectMapper.writeValueAsString(userSession));

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to update Hazelcast wallet cache for userId=" + userId, e);
        }
    }
}