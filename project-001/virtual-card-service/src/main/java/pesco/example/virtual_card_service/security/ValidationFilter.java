package pesco.example.virtual_card_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

@Component
public class ValidationFilter extends OncePerRequestFilter {

    // SQL Injection patterns
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
            "('|--|;|\\b(UNION|SELECT|INSERT|UPDATE|DELETE|DROP|EXEC)\\b|/\\*|\\*/|@@|#)",
            Pattern.CASE_INSENSITIVE
    );

    // XSS patterns
    private static final Pattern XSS_PATTERN = Pattern.compile(
            "(<script|javascript:|onerror=|onload=|onclick=|onmouseover=|alert\\(|document\\.cookie|<iframe|<img|<svg|eval\\()",
            Pattern.CASE_INSENSITIVE
    );

    // Path traversal patterns
    private static final Pattern PATH_TRAVERSAL_PATTERN = Pattern.compile(
            "(\\.\\./|\\.\\.\\\\)",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request, 0);
        String path = wrappedRequest.getRequestURI();

        if (path.contains("/auth/") || path.contains("/user/")) {
            String queryString = wrappedRequest.getQueryString();
            if (queryString != null && containsMaliciousInput(queryString)) {
                sendErrorResponse(response, "Invalid input detected in query parameters");
                return;
            }
            if (containsMaliciousInput(path)) {
                sendErrorResponse(response, "Invalid input detected in URL path");
                return;
            }

            if ("POST".equalsIgnoreCase(wrappedRequest.getMethod()) &&
                    (path.contains("/auth/login") || path.contains("/auth/register"))) {

                String requestBody = new String(wrappedRequest.getContentAsByteArray(), StandardCharsets.UTF_8);
                if (containsMaliciousInput(requestBody)) {
                    sendErrorResponse(response, "Invalid input detected in request body");
                    return;
                }
            }
        }
        // Continue filter chain with wrapped request
        filterChain.doFilter(wrappedRequest, response);
    }

    private boolean containsMaliciousInput(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }

        // SQL injection
        if (SQL_INJECTION_PATTERN.matcher(input).find()) return true;

        // XSS
        if (XSS_PATTERN.matcher(input).find()) return true;

        // Path traversal
        return PATH_TRAVERSAL_PATTERN.matcher(input).find();
    }

    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}
