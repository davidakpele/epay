package com.example.admin_api_service.components;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component("security")
public class SecurityExpressionMethods {

    public boolean isOwner(Long userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) return false;

        // "userId" matches the claim name in your token exactly
        Object tokenUserId = jwt.getClaim("userId");
        if (tokenUserId == null) return false;

        return userId.toString().equals(tokenUserId.toString());
    }
}