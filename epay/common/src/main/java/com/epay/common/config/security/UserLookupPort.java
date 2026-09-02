package com.epay.common.config.security;

import org.springframework.security.core.userdetails.UserDetails;
import java.util.Optional;
import java.util.UUID;

public interface UserLookupPort {

    Optional<UserDetails> findByUsername(String username);
    Optional<Long> findUserIdByUsername(String username);

    /** Look up a user by their UUID (the {@code sub} claim in new-format JWTs). */
    Optional<UserDetails> findByUserUuid(UUID userUuid);
}
