package com.pesco.wallet_service.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/error")
public class CustomErrorController {

    @GetMapping("/403")
    public String accessDeniedPage() {
        return "error/403"; 
    }


    @RequestMapping("/error")
    public ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        String requestUri = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

        int statusCode = status != null ? Integer.parseInt(status.toString()) : 500;
        HttpStatus httpStatus = HttpStatus.valueOf(statusCode);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", statusCode);
        errorResponse.put("error", httpStatus.getReasonPhrase());
        String errorMessage = getErrorMessage(statusCode, message, exception);
        errorResponse.put("message", errorMessage);
        errorResponse.put("path", requestUri != null ? requestUri : "unknown");
        return new ResponseEntity<>(errorResponse, httpStatus);
    }

    private String getErrorMessage(int statusCode, Object message, Object exception) {
        if (exception != null && exception.toString().contains("RequestRejectedException")) {
            return "The request was rejected due to security constraints";
        }
        
        if (message != null && message.toString().contains("potentially malicious")) {
            return "The request was rejected due to suspicious URL pattern";
        }
        return switch (statusCode) {
            case 400 -> message != null ? message.toString() : "Bad request";
            case 401 -> "Authentication required";
            case 403 -> "Access denied";
            case 404 -> "Resource not found";
            case 405 -> "Method not allowed";
            case 500 -> "Internal server error";
            default -> message != null ? message.toString() : "An error occurred";
        };
    }
}
