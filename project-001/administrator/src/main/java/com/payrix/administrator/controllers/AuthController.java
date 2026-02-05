package com.payrix.administrator.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/admin") 
public class AuthController {

    @GetMapping("/auth/login")
    public String login(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "expired", required = false) String expired,
            @RequestParam(value = "oauth_error", required = false) String oauthError,
            Model model) {
                
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/admin/dashboard";
        }

        if (error != null) {
            model.addAttribute("errorMessage", "Invalid username or password!");
        }
        
        if (logout != null) {
            model.addAttribute("successMessage", "You have been logged out successfully!");
        }
        
        if (expired != null) {
            model.addAttribute("errorMessage", "Your session has expired. Please login again.");
        }
        
        if (oauthError != null) {
            model.addAttribute("errorMessage", "OAuth2 authentication failed!");
        }
        
        return "auth/login";
    }
    
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/403";
    }

    @GetMapping("/login")
    public String redirectToAdminLogin() {
        return "redirect:/auth/login";
    }

    @GetMapping("/")
    public String Home() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/admin/dashboard";
        }
        return "auth/logout";
    }
    
    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        clearTokenCookies(response);
        
        return "auth/logout";
    }

    private void clearTokenCookies(HttpServletResponse response) {
        Cookie accessTokenCookie = new Cookie("access_token", null);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(0);
        Cookie refreshTokenCookie = new Cookie("refresh_token", null);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0);
        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);
    }

    

}
