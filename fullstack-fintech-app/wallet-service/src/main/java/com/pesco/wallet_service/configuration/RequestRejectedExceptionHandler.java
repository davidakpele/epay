package com.pesco.wallet_service.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class RequestRejectedExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @ExceptionHandler(RequestRejectedException.class)
    public void handleRequestRejectedException(
            RequestRejectedException ex,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("status", 400);
        errorDetails.put("error", "Bad Request");
        errorDetails.put("message", "The request was rejected due to suspicious URL pattern");
        errorDetails.put("details", sanitizeErrorMessage(ex.getMessage()));
        errorDetails.put("path", sanitizePath(request.getRequestURI()));

        response.getWriter().write(objectMapper.writeValueAsString(errorDetails));
    }

    private String sanitizeErrorMessage(String message) {
        if (message == null) {
            return "Invalid request format";
        }
        
        if (message.contains("//")) {
            return "URL contains invalid consecutive slashes";
        }
        if (message.contains("\\")) {
            return "URL contains invalid backslashes";
        }
        if (message.contains("%")) {
            return "URL contains invalid encoding";
        }
        if (message.contains(";")) {
            return "URL contains invalid semicolons";
        }
        
        return "Invalid request format";
    }

    private String sanitizePath(String path) {
        if (path == null) {
            return "/unknown";
        }
        String[] segments = path.split("/");
        if (segments.length > 1) {
            return "/" + segments[1];
        }
        return "/";
    }
}