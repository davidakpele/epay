package com.example.admin_api_service.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import com.example.admin_api_service.models.AdminUser;

@Component("security")
public class SecurityExpressionMethods {

    public boolean isOwner(Long resourceUserId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        Object principal = auth.getPrincipal();

        if (!(principal instanceof AdminUser user)) {
            return false;
        }

        return resourceUserId != null && resourceUserId.equals(user.getId());
    }

    public boolean isOwnerOrAdmin(Long resourceUserId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        Object principal = auth.getPrincipal();

        if (!(principal instanceof AdminUser user)) {
            return false;
        }

        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(a -> "ADMIN".equals(a.getAuthority()));

        return isAdmin || (resourceUserId != null && resourceUserId.equals(user.getId()));
    }
}