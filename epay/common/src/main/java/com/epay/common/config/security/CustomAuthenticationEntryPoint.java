package com.epay.common.config.security;

import com.epay.common.exception.ErrorCode;
import com.epay.common.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        log.warn("Unauthorized access to [{}]: {}", request.getRequestURI(), authException.getMessage());
        String errorCode    = getAttr(request, "jwt_error_code",    ErrorCode.UNAUTHORIZED_ACCESS);
        String errorMessage = getAttr(request, "jwt_error_message", "Authentication is required to access this resource");

        ErrorResponse body = ErrorResponse.builder()
                .success(false)
                .errorId(UUID.randomUUID().toString())
                .errorCode(errorCode)
                .message(errorMessage)
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .status(HttpStatus.UNAUTHORIZED.value())
                .build();

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), body);
    }

    private String getAttr(HttpServletRequest request, String attr, String fallback) {
        Object value = request.getAttribute(attr);
        return (value instanceof String s && !s.isBlank()) ? s : fallback;
    }
}
