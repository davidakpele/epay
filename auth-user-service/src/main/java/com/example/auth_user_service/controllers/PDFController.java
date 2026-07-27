package com.example.auth_user_service.controllers;
 
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.auth_user_service.interfaces.IPDFService;
import com.example.auth_user_service.payloads.StatementRequest;

@RestController
@RequestMapping("/receipt")
public class PDFController {

    private final IPDFService pdfService;

    public PDFController(IPDFService pdfService) {
        this.pdfService = pdfService;
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/generate-pdf/{userId}")
    public ResponseEntity<?> generatePDF(
            @PathVariable("userId") Long userId,
            @RequestBody StatementRequest request) {
        try {
            pdfService.generateAndSendBankStatement(
                request.getEmail(), "Customer", request.getStatements()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "PDF generated and sent successfully");
            response.put("email", request.getEmail());
            response.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // Get root cause
            Throwable cause = e.getCause() != null ? e.getCause() : e;

            // Log full stack trace to server console
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Error generating/sending PDF");
            errorResponse.put("error", cause.getClass().getSimpleName() + ": " + cause.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

}
