package com.epay.common.interfaces;

public interface IBlacklistPort {

    boolean isAccountBlacklisted(Long userId);

    boolean isIpBlacklisted(String ipAddress);

    boolean isAccountNumberBlacklisted(String accountNumber);
}
