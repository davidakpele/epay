package com.epay.common.config.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.epay.domain.auth.entity.User;

@Component("security")
public class SecurityExpressionMethods {

    public boolean isOwner(Long resourceUserId) {
        return isOwnerOrAdmin(resourceUserId);
    }

    public boolean isOwnerOrAdmin(Long resourceUserId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) return false;

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> "ADMIN".equals(a.getAuthority()));
        if (isAdmin) return true;

        if (resourceUserId == null) return false;

        Object principal = auth.getPrincipal();

        if (principal instanceof org.springframework.security.oauth2.jwt.Jwt jwt) {
            Object userIdClaim = jwt.getClaim("userId");
            if (userIdClaim == null) return false;
            return resourceUserId.equals(Long.valueOf(userIdClaim.toString()));
        }

        if (principal instanceof User user) {
            return resourceUserId.equals(user.getId());
        }

        return false;
    }
}