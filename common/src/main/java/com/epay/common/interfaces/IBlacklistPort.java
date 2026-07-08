package com.epay.common.interfaces;

/**
 * Port for checking blacklist status before processing withdrawals.
 * Implemented by BlacklistCheckAdapter in epay-blacklist.
 */
public interface IBlacklistPort {

    boolean isAccountBlacklisted(Long userId);

    boolean isIpBlacklisted(String ipAddress);

    boolean isAccountNumberBlacklisted(String accountNumber);
}
