package com.epay.common.exception;

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
public class JwtExceptionHandler implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        log.warn("Unauthorized access attempt to [{}] - {}",
                request.getRequestURI(), resolveReason(request));

        HttpStatus status = HttpStatus.UNAUTHORIZED;
        String errorCode = resolveErrorCode(request);
        String message   = resolveMessage(request);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .errorId(UUID.randomUUID().toString())
                .errorCode(errorCode)
                .message(message)
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .status(status.value())
                .build();

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }

    private String resolveErrorCode(HttpServletRequest request) {
        String jwtError = (String) request.getAttribute("jwt_error_code");
        return jwtError != null ? jwtError : ErrorCode.UNAUTHORIZED_ACCESS;
    }

    private String resolveMessage(HttpServletRequest request) {
        String jwtMessage = (String) request.getAttribute("jwt_error_message");
        return jwtMessage != null ? jwtMessage : "Authentication is required to access this resource";
    }

    private String resolveReason(HttpServletRequest request) {
        String reason = (String) request.getAttribute("jwt_error_message");
        return reason != null ? reason : "No authentication token provided";
    }
}
