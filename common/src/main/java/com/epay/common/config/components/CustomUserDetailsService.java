package com.epay.common.config.components;

import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.epay.common.config.security.UserLookupPort;

@Service
@Primary
public class CustomUserDetailsService implements UserDetailsService {

    private final UserLookupPort userLookupPort;

    public CustomUserDetailsService(UserLookupPort userLookupPort) {
        this.userLookupPort = userLookupPort;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        return userLookupPort.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found with username: " + username));
    }
}