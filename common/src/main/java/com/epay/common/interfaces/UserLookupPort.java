package com.epay.common.interfaces;

import java.util.Optional;

public interface UserLookupPort {

    Optional<Long> findUserIdByUsername(String username);

    boolean existsActiveUser(Long userId);

    /** Returns the email for a given userId — used by deposit/notification to address emails. */
    Optional<String> findEmailByUserId(Long userId);

    /** Returns first + last name for a given userId — used for notification greeting. */
    Optional<String> findFullNameByUserId(Long userId);
}
