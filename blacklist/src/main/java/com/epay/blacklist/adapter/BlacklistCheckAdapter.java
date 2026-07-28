package com.epay.blacklist.adapter;

import com.epay.blacklist.service.BlacklistService;
import com.epay.common.interfaces.IBlacklistPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BlacklistCheckAdapter implements IBlacklistPort {

    private final BlacklistService blacklistService;

    @Override
    public boolean isAccountBlacklisted(Long userId) {
        return blacklistService.isAccountBlacklisted(userId);
    }

    @Override
    public boolean isIpBlacklisted(String ipAddress) {
        return blacklistService.isIpBlacklisted(ipAddress);
    }

    @Override
    public boolean isAccountNumberBlacklisted(String accountNumber) {
        return blacklistService.isAccountNumberBlacklisted(accountNumber);
    }
}
