package com.epay.wallet.port;

import java.util.Optional;

/**
 * Port that wallet module uses to validate users without importing the auth module.
 *
 * Defined here in wallet.
 * Implemented in auth via WalletUserLookupAdapter.
 *
 * Dependency direction stays:   common ← auth ← wallet  (wallet never imports auth)
 */
public interface UserLookupPort {

    /**
     * Returns the userId if an active, enabled account exists for this username.
     */
    Optional<Long> findUserIdByUsername(String username);

    /**
     * Returns true if an active, enabled account exists for this userId.
     */
    boolean existsActiveUser(Long userId);
}
