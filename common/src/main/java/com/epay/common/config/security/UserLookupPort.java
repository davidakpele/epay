package com.epay.common.config.security;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

/**
 * Port (interface) that decouples the security layer from the auth module.
 *
 * common defines this interface.
 * auth implements it via {@code UserLookupAdapter} which calls {@code UserRepository}.
 *
 * This prevents common from depending on auth — the dependency arrow stays:
 *   common  ←  auth  (auth imports common, never the reverse)
 */
public interface UserLookupPort {

    Optional<UserDetails> findByUsername(String username);
}
