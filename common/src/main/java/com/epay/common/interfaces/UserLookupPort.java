package com.epay.common.interfaces;

import java.util.Optional;

public interface UserLookupPort {

    Optional<Long> findUserIdByUsername(String username);
    boolean existsActiveUser(Long userId);
}
