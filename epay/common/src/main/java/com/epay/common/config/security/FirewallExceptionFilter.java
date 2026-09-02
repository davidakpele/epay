package com.epay.common.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class FirewallExceptionFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            filterChain.doFilter(request, response);
        } catch (RequestRejectedException ex) {

            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setContentType("application/json");

            Map<String, Object> error = new HashMap<>();
            error.put("timestamp", LocalDateTime.now().toString());
            error.put("status", 400);
            error.put("error", "Bad Request");
            error.put("message", "Request blocked by firewall");
            error.put("details", sanitizeMessage(ex.getMessage()));
            error.put("path", request.getRequestURI());

            response.getWriter().write(objectMapper.writeValueAsString(error));
        }
    }

    private String sanitizeMessage(String message) {
        if (message == null) return "Invalid request";

        if (message.contains("//")) return "URL contains consecutive slashes";
        if (message.contains("\\")) return "URL contains backslashes";
        if (message.contains("%")) return "Invalid percent encoding";
        if (message.contains(";")) return "Forbidden character detected";

        return "Potentially malicious request";
    }
}
