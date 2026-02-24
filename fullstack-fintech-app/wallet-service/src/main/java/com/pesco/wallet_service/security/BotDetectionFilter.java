package com.pesco.wallet_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class BotDetectionFilter extends OncePerRequestFilter {

    private static final List<String> BLOCKED_USER_AGENTS = Arrays.asList(
        "python-requests",
        "curl",
        "wget",
        "sqlmap",
        "nikto",
        "nessus",
        "hydra",
        "scrapy",
        "bot",
        "crawler",
        "spider"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/ws/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        
        String userAgent = request.getHeader("User-Agent");
        
        // Block empty or missing user agent
        if (userAgent == null || userAgent.trim().isEmpty()) {
            blockBot(response, "Missing User-Agent");
            return;
        }
        
        // Check against known bot signatures
        String lowerUserAgent = userAgent.toLowerCase();
        for (String blockedAgent : BLOCKED_USER_AGENTS) {
            if (lowerUserAgent.contains(blockedAgent.toLowerCase())) {
                blockBot(response, "Suspicious User-Agent: " + blockedAgent);
                return;
            }
        }
        
        // Allow legitimate search engine bots on specific paths only
        if (isSearchEngineBot(lowerUserAgent)) {
            String path = request.getRequestURI();
            // Only allow bots on public paths
            if (!path.startsWith("/public") && !path.equals("/") && 
                !path.startsWith("/robots.txt") && !path.startsWith("/sitemap")) {
                blockBot(response, "Search engine bot on restricted path");
                return;
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    private boolean isSearchEngineBot(String userAgent) {
        return userAgent.contains("googlebot") || 
               userAgent.contains("bingbot") || 
               userAgent.contains("slurp");
    }
    
    private void blockBot(HttpServletResponse response, String reason) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
            String.format("{\"error\":\"Bot detected\",\"message\":\"%s\",\"status\":403}", reason)
        );
    }
}