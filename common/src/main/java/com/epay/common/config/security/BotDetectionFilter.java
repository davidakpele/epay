package com.epay.common.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Blocks known malicious scanning tools.
 * curl/wget are only blocked in production — they are legitimate API testing tools in dev.
 */
@Slf4j
@Component
public class BotDetectionFilter extends OncePerRequestFilter {

    /** Always blocked regardless of environment. */
    private static final List<String> ALWAYS_BLOCKED = List.of(
            "sqlmap", "nikto", "nessus", "hydra", "zgrab", "masscan",
            "nmap", "dirbuster", "gobuster", "nuclei", "acunetix"
    );

    /** Only blocked in production. */
    private static final List<String> PROD_ONLY_BLOCKED = List.of(
            "python-requests", "scrapy", "mechanize", "libwww-perl"
    );

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Never filter WebSocket upgrades or health probes
        return path.startsWith("/ws/") || path.equals("/actuator/health");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String userAgent = request.getHeader("User-Agent");

        if (userAgent == null || userAgent.trim().isEmpty()) {
            log.warn("[BOT] Blocked request with missing User-Agent from IP: {}",
                    extractIp(request));
            blockBot(response, "Missing User-Agent header");
            return;
        }

        String ua = userAgent.toLowerCase();

        // Always block known attack tools
        for (String blocked : ALWAYS_BLOCKED) {
            if (ua.contains(blocked)) {
                log.warn("[BOT] Blocked known attack tool '{}' from IP: {}", blocked, extractIp(request));
                blockBot(response, "Automated scanning tool detected");
                return;
            }
        }

        // Block scrapers only in production
        boolean isProduction = activeProfile.contains("prod");
        if (isProduction) {
            for (String blocked : PROD_ONLY_BLOCKED) {
                if (ua.contains(blocked)) {
                    log.warn("[BOT] Blocked scraper '{}' in production from IP: {}", blocked, extractIp(request));
                    blockBot(response, "Automated client blocked in production");
                    return;
                }
            }
        }

        // Allow legitimate search engine bots only on public paths
        if (isSearchEngineBot(ua)) {
            String path = request.getRequestURI();
            if (!path.startsWith("/public") && !path.equals("/")
                    && !path.equals("/robots.txt") && !path.startsWith("/sitemap")) {
                log.debug("[BOT] Search engine bot blocked on restricted path: {}", path);
                blockBot(response, "Bot access restricted to public paths");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isSearchEngineBot(String ua) {
        return ua.contains("googlebot") || ua.contains("bingbot") || ua.contains("slurp");
    }

    private void blockBot(HttpServletResponse response, String reason) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                String.format("{\"success\":false,\"status\":403,\"message\":\"%s\"}", reason));
    }

    private String extractIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
        return request.getRemoteAddr();
    }
}
