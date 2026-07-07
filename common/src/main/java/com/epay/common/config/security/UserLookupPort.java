package com.epay.common.config.security;

import org.springframework.security.core.userdetails.UserDetails;
import java.util.Optional;

public interface UserLookupPort {

    Optional<UserDetails> findByUsername(String username);
    Optional<Long> findUserIdByUsername(String username);
}
