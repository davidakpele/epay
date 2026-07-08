package com.epay.common.interfaces;

import java.util.Optional;

public interface UserLookupPort {

    Optional<Long> findUserIdByUsername(String username);

    boolean existsActiveUser(Long userId);
    Optional<String> findEmailByUserId(Long userId);

    Optional<String> findFullNameByUserId(Long userId);
}
