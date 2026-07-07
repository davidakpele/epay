package com.epay.common.config.security;

import org.springframework.security.core.userdetails.UserDetails;

import com.epay.domain.auth.entity.User;

import java.util.Optional;


public interface UserLookupPort {

    Optional<UserDetails> findByUsername(String username);
    Optional<User> findUserIdByUsername(String username);
}
