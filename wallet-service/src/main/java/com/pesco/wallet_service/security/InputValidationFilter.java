package com.pesco.wallet_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.regex.Pattern;

@Component
public class InputValidationFilter extends OncePerRequestFilter {

    private static final Pattern PATH_TRAVERSAL_PATTERN = Pattern.compile(
        "(\\.\\./|\\.\\\\|%2e%2e%2f|%2e%2e/|%2e%2e%5c|\\.\\.\\/|/\\.\\./|\\\\\\.\\.\\\\)", 
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        "(union.{0,5}select|insert.{0,5}into|delete.{0,5}from|" +
        "drop.{0,5}table|exec\\(|execute\\(|sp_|xp_|--|/\\*|\\*/|" +
        "<script|javascript:|onerror=|onload=|alert\\(|eval\\(|base64)",
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern XSS_PATTERN = Pattern.compile(
        "(<script|javascript:|onerror|onload|onclick|onmouseover|onfocus|onblur|<iframe|<object|" +
        "<embed|vbscript:|data:text/html|eval\\(|expression\\(|@import|<link|<style)", 
        Pattern.CASE_INSENSITIVE
    );

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String fullUrl = uri + (queryString != null ? "?" + queryString : "");
        
        // Check for path traversal
        if (PATH_TRAVERSAL_PATTERN.matcher(fullUrl).find()) {
            blockRequest(response, "Path traversal attempt detected");
            return;
        }
        
        // Check for SQL injection
        if (SQL_INJECTION_PATTERN.matcher(fullUrl).find()) {
            blockRequest(response, "SQL injection attempt detected");
            return;
        }
        
        // Check for XSS
        if (XSS_PATTERN.matcher(fullUrl).find()) {
            blockRequest(response, "XSS attempt detected");
            return;
        }
        
        // Block suspicious characters
        if (fullUrl.contains("'") || fullUrl.contains("\"") || 
            fullUrl.contains(";") || fullUrl.contains("--") ||
            fullUrl.contains("/*") || fullUrl.contains("*/")) {
            blockRequest(response, "Suspicious characters detected");
            return;
        }
        
        filterChain.doFilter(request, response);
    }
    
    private void blockRequest(HttpServletResponse response, String reason) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
            String.format("{\"error\":\"Security violation\",\"message\":\"%s\",\"status\":400}", reason)
        );
    }
}