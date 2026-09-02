package com.epay.domain.common.exception;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ErrorHandler {
        public ResponseEntity<Map<String, Object>> error(String message, HttpStatus status, String detail) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("success", false);
            body.put("status",  status.value());
            body.put("message", message);
            body.put("detail",  detail);
            return ResponseEntity.status(status).body(body);
        }
}
