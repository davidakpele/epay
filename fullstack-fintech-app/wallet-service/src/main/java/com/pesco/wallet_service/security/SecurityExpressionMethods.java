package com.pesco.wallet_service.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.pesco.wallet_service.dtos.UserDTO;

@Component("security") 
public class SecurityExpressionMethods {

    public boolean isOwner(Long resourceUserId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        Object principal = auth.getPrincipal();

        if (!(principal instanceof UserDTO user)) {
            return false;
        }

        return resourceUserId != null && resourceUserId.equals(user.getId());
    }
    
    public boolean isOwnerOrAdmin(Long resourceUserId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> "ADMIN".equals(a.getAuthority()));

        if (isAdmin) {
            return true;
        }

        Object principal = auth.getPrincipal();

        if (!(principal instanceof UserDTO user)) {
            return false;
        }

        return resourceUserId != null && resourceUserId.equals(user.getId());
    }
}
