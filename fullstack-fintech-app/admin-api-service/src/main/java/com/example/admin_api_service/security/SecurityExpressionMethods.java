package com.example.admin_api_service.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import com.example.admin_api_service.models.AdminUser;

@Component("security")
public class SecurityExpressionMethods {

    public boolean isOwner(Long resourceUserId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;
        AdminUser user = (AdminUser) auth.getPrincipal();
        return user.getId().equals(resourceUserId);
    }

    public boolean isOwnerOrAdmin(Long resourceUserId) {  // ← add this
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;
        AdminUser user = (AdminUser) auth.getPrincipal();
        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN"));
        return isAdmin || user.getId().equals(resourceUserId);
    }
}
